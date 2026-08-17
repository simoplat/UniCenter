package it.project.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import it.project.ConsoleUI;
import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.observer.NotificaEsitoObserver;

/**
 * Controller (GRASP / Facade Controller) per il UC3 - Accettazione/Rifiuto Voto Esame.
 *
 * Riceve le richieste dalla UI (o dal job schedulato per la gestione scadenze)
 * e coordina l'elaborazione: pubblicazione esiti, accettazione/rifiuto voti,
 * verifica scadenze (silenzio-rifiuto), e registrazione nel libretto.
 */
public class GestioneVotoController {

    private final List<EsameSostenuto> esitiPubblicati;
    private final AtomicInteger contatorEsami;
    private final Unicenter unicenter;
    private final GestoreMaterieController gestoreMaterie;
    private final ConsoleUI console = ConsoleUI.getInstance();

    public GestioneVotoController(Unicenter unicenter, GestoreMaterieController gestoreMaterie) {
        this.unicenter = unicenter;
        this.gestoreMaterie = gestoreMaterie;
        this.esitiPubblicati = new ArrayList<>();
        this.contatorEsami = new AtomicInteger(0);
    }

    // =========================================================================
    // PUBBLICAZIONE ESITO (Professore)
    // =========================================================================

    /**
     * Il Professore pubblica l'esito di un esame.
     * Se il voto è >= 18: stato "In attesa di conferma" (Regola di Dominio 4 applicata in EsameSostenuto).
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

        // Recupera i CFU della materia
        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria);
        if (materia == null) {
            throw new IllegalArgumentException("Materia non trovata: " + codiceMateria);
        }
        int cfu = materia.getCfu();

        // Genera ID univoco
        String idEsame = "ESM-" + String.format("%05d", contatorEsami.incrementAndGet());

        // Crea l'EsameSostenuto (RD4 applicata nel costruttore)
        EsameSostenuto esame = new EsameSostenuto(
                idEsame, codiceAppello, matricolaStudente,
                codiceMateria, idProfessore,
                votoNumerico, lode, cfu, giorniScadenza
        );

        // Registra l'Observer (notifica Studente e Professore)
        Studente studente = unicenter.trovaStudente(matricolaStudente).orElse(null);
        Professore professore = trovaProfessore(idProfessore);

        if (studente != null && professore != null) {
            NotificaEsitoObserver observer = new NotificaEsitoObserver(studente, professore);
            esame.aggiungiOsservatore(observer);
        }

        esitiPubblicati.add(esame);

        // Se bocciato, notifica subito
        if (esame.getNomeStato().equals("Bocciato")) {
            console.mostraMessaggio("[SISTEMA] Voto " + votoNumerico
                    + " insufficiente per " + matricolaStudente
                    + ": esame registrato come BOCCIATO.");
        }

        return esame;
    }

    // =========================================================================
    // ACCETTAZIONE / RIFIUTO VOTO (Studente)
    // =========================================================================

    /**
     * Lo Studente accetta il voto. Il voto diventa definitivo ("Approvato")
     * e viene registrato nel libretto dello studente.
     *
     * @param idEsame l'identificativo dell'esame sostenuto
     * @return true se l'operazione è riuscita
     */
    public boolean accettaVoto(String idEsame) {
        EsameSostenuto esame = trovaEsameById(idEsame);
        if (esame == null) {
            console.mostraErrore("Esame non trovato: " + idEsame);
            return false;
        }

        try {
            esame.accetta();

            // Registra nel libretto dello studente (Information Expert)
            Studente studente = unicenter.trovaStudente(esame.getMatricolaStudente()).orElse(null);
            if (studente != null) {
                studente.getLibretto().registraEsame(esame);
            }

            return true;
        } catch (IllegalStateException e) {
            console.mostraErrore(e.getMessage());
            return false;
        }
    }

    /**
     * Lo Studente rifiuta il voto. Lo stato diventa "Rifiutato"
     * e il voto non viene verbalizzato.
     *
     * @param idEsame l'identificativo dell'esame sostenuto
     * @return true se l'operazione è riuscita
     */
    public boolean rifiutaVoto(String idEsame) {
        EsameSostenuto esame = trovaEsameById(idEsame);
        if (esame == null) {
            console.mostraErrore("Esame non trovato: " + idEsame);
            return false;
        }

        try {
            esame.rifiuta();
            return true;
        } catch (IllegalStateException e) {
            console.mostraErrore(e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // ESTENSIONE A: SILENZIO RIFIUTO (Job Schedulato)
    // =========================================================================

    /**
     * Verifica tutti gli esiti in "In attesa di conferma" e applica il rifiuto automatico
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
                    console.mostraMessaggio("[SISTEMA] Silenzio-rifiuto applicato per esame "
                            + esame.getIdEsame() + " (studente: " + esame.getMatricolaStudente()
                            + ", materia: " + esame.getCodiceMateria() + ")");
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
     * Restituisce tutti gli esiti pendenti ("In attesa di conferma") per uno studente.
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

    private EsameSostenuto trovaEsameById(String idEsame) {
        for (EsameSostenuto esame : esitiPubblicati) {
            if (esame.getIdEsame().equals(idEsame)) {
                return esame;
            }
        }
        return null;
    }

    private Professore trovaProfessore(String idProfessore) {
        List<Studente> studenti = unicenter.getStudentiIscritti();
        // Cerchiamo tra gli utenti generici (hack: usiamo il metodo di Unicenter)
        // Il professore non è tra gli studenti, usiamo il metodo di ricerca generico
        return unicenter.trovaProfessore(idProfessore).orElse(null);
    }
}
