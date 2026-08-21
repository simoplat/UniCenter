package it.project.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.exceptions.EsameNonTrovatoException;
import it.project.generator.IdVerbaleGenerator;
import it.project.observer.NotificaEsitoObserver;

/**
 * Controller (GRASP / Facade Controller) per il UC3 - Accettazione/Rifiuto Voto
 * Esame.
 *
 * Riceve le richieste dalla UI (o dal job schedulato per la gestione scadenze)
 * e coordina l'elaborazione: pubblicazione esiti, accettazione/rifiuto voti,
 * verifica scadenze (silenzio-rifiuto), e registrazione nel libretto.
 */
public class GestioneVotoController {

    private final List<EsameSostenuto> esitiPubblicati;
    private final IdVerbaleGenerator idVerbaleGenerator;
    private final Unicenter unicenter;
    private final GestoreMaterieController gestoreMaterie;

    public GestioneVotoController(Unicenter unicenter, GestoreMaterieController gestoreMaterie) {
        this.unicenter = unicenter;
        this.gestoreMaterie = gestoreMaterie;
        this.esitiPubblicati = new ArrayList<>();
        this.idVerbaleGenerator = IdVerbaleGenerator.getInstance();
    }

    // =========================================================================
    // PUBBLICAZIONE ESITO (Professore)
    // =========================================================================

    /**
     * Il Professore pubblica l'esito di un esame.
     * Se il voto è >= 18: stato "In attesa di conferma" (Regola di Dominio 4
     * applicata in EsameSostenuto).
     * Se il voto è < 18: stato "Bocciato" automaticamente.
     *
     * @param codiceAppello     codice dell'appello
     * @param matricolaStudente matricola dello studente
     * @param codiceMateria     codice della materia
     * @param votoNumerico      il voto numerico assegnato
     * @param lode              true se 30 e lode
     * @param giorniScadenza    giorni entro cui lo studente deve confermare
     * @return l'EsameSostenuto creato
     */
    public EsameSostenuto pubblicaEsito(String codiceAppello, String matricolaStudente,
            String codiceMateria, String idProfessore,
            int votoNumerico, boolean lode, int giorniScadenza) {

        // Validazioni
        if (lode && votoNumerico != 30) {
            throw new IllegalArgumentException("La lode può essere assegnata solo con voto 30.");
        }
        if (votoNumerico < 0 || votoNumerico > 30) {
            throw new IllegalArgumentException("Il voto deve essere compreso tra 0 e 30.");
        }

        // Controlla che non esista già un esito per lo stesso studente sullo stesso
        // appello
        for (EsameSostenuto esistente : esitiPubblicati) {
            if (esistente.getCodiceAppello().equals(codiceAppello)
                    && esistente.getMatricolaStudente().equals(matricolaStudente)) {
                throw new IllegalStateException(
                        "Esito già registrato per lo studente " + matricolaStudente
                                + " sull'appello " + codiceAppello + ".");
            }
        }

        // Recupera i CFU della materia
        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria);
        if (materia == null) {
            throw new IllegalArgumentException("Materia non trovata: " + codiceMateria);
        }
        int cfu = materia.getCfu();

        // Genera ID verbale univoco
        String idVerbale = idVerbaleGenerator.generateId();

        // Crea l'EsameSostenuto (RD4 applicata nel costruttore)
        EsameSostenuto esame = new EsameSostenuto(
                idVerbale, codiceAppello, matricolaStudente,
                codiceMateria, idProfessore,
                votoNumerico, lode, cfu, giorniScadenza);

        // Registra l'Observer (notifica Studente e Professore)
        Studente studente = unicenter.trovaStudente(matricolaStudente).orElse(null);
        Professore professore = trovaProfessore(idProfessore);

        if (studente != null && professore != null) {
            NotificaEsitoObserver observer = new NotificaEsitoObserver(studente, professore);
            esame.aggiungiOsservatore(observer);

            // Notifica iniziale alla pubblicazione dell'esito (l'observer viene registrato
            // dopo il costruttore, quindi lo stato iniziale non era mai notificato)
            observer.aggiornamento(esame, esame.getNomeStato());
        }

        esitiPubblicati.add(esame);

        return esame;
    }

    // =========================================================================
    // ACCETTAZIONE / RIFIUTO VOTO (Studente)
    // =========================================================================

    /**
     * Lo Studente accetta il voto. Il voto diventa definitivo ("Approvato")
     * e viene registrato nel libretto dello studente.
     *
     * @param idVerbale l'identificativo del verbale dell'esame sostenuto
     * @return true se l'operazione è riuscita
     */
    public boolean accettaVoto(String idVerbale) {
        EsameSostenuto esame = trovaEsameById(idVerbale);

        // Recupera lo studente e verifica la presenza
        Studente studente = unicenter.trovaStudente(esame.getMatricolaStudente())
                .orElseThrow(() -> new IllegalStateException(
                        "Studente non trovato per la matricola: " + esame.getMatricolaStudente()));

        if (studente.getLibretto() == null) {
            throw new IllegalStateException("Libretto non disponibile per lo studente " + esame.getMatricolaStudente());
        }

        esame.accetta();

        // Registra nel libretto dello studente (Information Expert)
        studente.getLibretto().registraEsame(esame);

        // Rimuove gli altri esiti pendenti della stessa materia per lo stesso studente
        String matricola = esame.getMatricolaStudente();
        String codiceMateria = esame.getCodiceMateria();
        esitiPubblicati.removeIf(e -> !e.getIdVerbale().equals(idVerbale)
                && e.getMatricolaStudente().equals(matricola)
                && e.getCodiceMateria().equals(codiceMateria)
                && e.getNomeStato().equals("In attesa di conferma"));

        return true;
    }

    /**
     * Lo Studente rifiuta il voto. Lo stato diventa "Rifiutato"
     * e il voto non viene verbalizzato.
     *
     * @param idVerbale l'identificativo del verbale dell'esame sostenuto
     * @return true se l'operazione è riuscita
     */
    public boolean rifiutaVoto(String idVerbale) {
        EsameSostenuto esame = trovaEsameById(idVerbale);
        esame.rifiuta();
        return true;
    }

    // =========================================================================
    // ESTENSIONE A: SILENZIO RIFIUTO (Job Schedulato)
    // =========================================================================

    /**
     * Verifica tutti gli esiti in "In attesa di conferma" e applica il rifiuto
     * automatico
     * se la scadenza temporale è passata (Estensione A - Silenzio Rifiuto).
     *
     * @return il numero di esiti rifiutati automaticamente
     */
    public int verificaScadenze() {
        int contatore = 0;
        for (EsameSostenuto esame : esitiPubblicati) {
            if (esame.isScaduto()) {
                try {
                    esame.rifiuta();
                    contatore++;
                } catch (IllegalStateException e) {
                    // Stato già gestito, ignora
                }
            }
        }
        return contatore;
    }

    // =========================================================================
    // QUERY
    // =========================================================================

    /**
     * Restituisce tutti gli esiti pendenti ("In attesa di conferma") per uno
     * studente.
     */
    public List<EsameSostenuto> trovaEsitiPendentiByStudente(String matricola) {
        List<EsameSostenuto> risultato = new ArrayList<>();
        for (EsameSostenuto esame : esitiPubblicati) {
            if (esame.getMatricolaStudente().equals(matricola)
                    && esame.getNomeStato().equals("In attesa di conferma")) {
                risultato.add(esame);
            }
        }
        return risultato;
    }

    /**
     * Restituisce tutti gli esiti di uno studente (qualsiasi stato).
     */
    public List<EsameSostenuto> trovaEsitiByStudente(String matricola) {
        List<EsameSostenuto> risultato = new ArrayList<>();
        for (EsameSostenuto esame : esitiPubblicati) {
            if (esame.getMatricolaStudente().equals(matricola)) {
                risultato.add(esame);
            }
        }
        return risultato;
    }

    /**
     * Restituisce tutti gli esiti pubblicati da un professore.
     */
    public List<EsameSostenuto> trovaEsitiByProfessore(String idProfessore) {
        List<EsameSostenuto> risultato = new ArrayList<>();
        for (EsameSostenuto esame : esitiPubblicati) {
            if (esame.getIdProfessore().equals(idProfessore)) {
                risultato.add(esame);
            }
        }
        return risultato;
    }

    /**
     * Restituisce tutti gli esiti pubblicati (per debug/admin).
     */
    public List<EsameSostenuto> getTuttiGliEsiti() {
        return Collections.unmodifiableList(esitiPubblicati);
    }

    // =========================================================================
    // METODI AUSILIARI
    // =========================================================================

    private EsameSostenuto trovaEsameById(String idVerbale) {
        if (idVerbale != null) {
            for (EsameSostenuto esame : esitiPubblicati) {
                if (esame.getIdVerbale().equals(idVerbale)) {
                    return esame;
                }
            }
        }
        throw new EsameNonTrovatoException("Verbale d'esame non trovato: " + idVerbale);
    }

    private Professore trovaProfessore(String idProfessore) {
        return unicenter.trovaProfessore(idProfessore)
                .orElseThrow(() -> new IllegalArgumentException("Professore non trovato con ID: " + idProfessore));
    }
}
