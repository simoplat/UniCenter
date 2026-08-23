package it.project.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.project.CorsoDiLaurea;
import it.project.Materia;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.factory.CorsoDiLaureaFactory;

/**
 * Controller (GRASP / Facade Controller) per UC4/UC5 - Gestione Corsi di Laurea
 * e Materie.
 * Coordina le operazioni dell'amministratore relative ai percorsi universitari:
 * creazione, aggiornamento, obsolescenza, finalizzazione e ricerca dei Corsi di
 * Laurea.
 */
public class GestioneCorsiLaureaController {
    private final List<CorsoDiLaurea> corsiDiLaurea;

    /**
     * Costruttore di default. Inizializza una lista vuota di corsi di laurea.
     */
    public GestioneCorsiLaureaController() {
        this.corsiDiLaurea = new ArrayList<>();
    }

    // =========================================================================
    // OPERAZIONI CRUD (UC4)
    // =========================================================================

    /**
     * Crea un nuovo Corso di Laurea tramite la Factory e lo aggiunge al sistema.
     * La Factory applica le validazioni e genera il codice univoco (Regola di
     * Dominio 3).
     * Il corso viene creato come "non finalizzato": non ha materie e non è visibile
     * per l'immatricolazione.
     *
     * @param nome           denominazione del corso
     * @param tipologia      tipologia del corso
     * @param anniAccademici durata in anni
     * @return il CorsoDiLaurea appena creato
     */
    public CorsoDiLaurea creaCorsoDiLaurea(String nome, String tipologia, int anniAccademici) {
        // Controlla duplicati per nome
        boolean giaPresente = corsiDiLaurea.stream()
                .anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
        if (giaPresente) {
            throw new IllegalArgumentException(
                    "Esiste già un corso di laurea con il nome '" + nome + "'.");
        }

        // Delega la creazione alla Factory (validazione + generazione codice)
        CorsoDiLaurea nuovoCorso = CorsoDiLaureaFactory.creaCorsoDiLaurea(nome, tipologia, anniAccademici);
        corsiDiLaurea.add(nuovoCorso);
        return nuovoCorso;
    }

    /**
     * Aggiorna i dati di un Corso di Laurea esistente.
     *
     * @param codice         codice identificativo del corso da aggiornare
     * @param nuovoNome      nuovo nome (null o vuoto per non modificare)
     * @param nuovaTipologia nuova tipologia (null o vuoto per non modificare)
     * @return true se l'aggiornamento è andato a buon fine
     */
    public boolean aggiornaCorsoDiLaurea(String codice, String nuovoNome, String nuovaTipologia) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaById(codice);

        if (corso.isObsoleto()) {
            throw new IllegalStateException("Impossibile aggiornare un corso obsoleto (codice: " + codice + ").");
        }

        if (corso.isFinalizzato()) {
            throw new IllegalStateException(
                    "Impossibile aggiornare il corso '" + corso.getNome() + "': è già finalizzato. "
                            + "È possibile solo renderlo obsoleto e crearne uno nuovo.");
        }

        if (nuovoNome != null && !nuovoNome.trim().isEmpty()) {
            // Verifica che il nuovo nome non sia già usato da un altro corso
            boolean nomeGiaUsato = corsiDiLaurea.stream()
                    .anyMatch(
                            c -> c.getNome().equalsIgnoreCase(nuovoNome.trim()) && !c.getId().equalsIgnoreCase(codice));
            if (nomeGiaUsato) {
                throw new IllegalArgumentException(
                        "Esiste già un altro corso con il nome '" + nuovoNome + "'.");
            }
            corso.setNome(nuovoNome.trim());
        }

        if (nuovaTipologia != null && !nuovaTipologia.trim().isEmpty()) {
            int nuoviAnni = CorsoDiLaureaFactory.getAnniPerTipologia(nuovaTipologia);
            corso.setTipologia(nuovaTipologia);
            corso.setAnniAccademici(nuoviAnni);
        }

        return true;
    }

    /**
     * Rende obsoleto un Corso di Laurea (soft-delete).
     * I corsi obsoleti non accettano nuove iscrizioni ma restano nel sistema.
     *
     * @param codice codice del corso
     * @return true se reso obsoleto con successo
     */
    public boolean rendiObsoletoCorsoDiLaurea(String codice) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaById(codice);

        if (corso.isObsoleto()) {
            throw new IllegalStateException("Il corso '" + corso.getNome() + "' è già obsoleto.");
        }

        corso.rendiObsoleto();
        return true;
    }

    /**
     * Elimina definitivamente un Corso di Laurea (se non ancora finalizzato o se
     * già reso obsoleto).
     * I corsi attivi e finalizzati devono essere prima resi obsoleti per sicurezza.
     *
     * @param codice codice identificativo del corso da eliminare
     * @return true se eliminato con successo
     */
    public boolean eliminaCorsoDiLaurea(String codice) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaById(codice);

        if (corso.isFinalizzato() && !corso.isObsoleto()) {
            throw new IllegalStateException(
                    "Impossibile eliminare un corso di laurea attivo e finalizzato. Rendi prima il corso obsoleto.");
        }

        return corsiDiLaurea.remove(corso);
    }

    // =========================================================================
    // UC5 - FINALIZZAZIONE CORSO DI LAUREA
    // =========================================================================

    /**
     * Restituisce i corsi creati ma non ancora finalizzati (senza materie
     * associate).
     * Questi sono i corsi che l'amministratore può ancora configurare.
     *
     * @return lista corsi non finalizzati
     */
    public List<CorsoDiLaurea> getCorsiNonFinalizzati() {
        return corsiDiLaurea.stream()
                .filter(c -> !c.isFinalizzato() && !c.isObsoleto())
                .collect(Collectors.toList());
    }

    /**
     * Associa una materia a un anno specifico di un Corso di Laurea.
     * Delega al CorsoDiLaurea la validazione dell'anno e dello stato di
     * finalizzazione
     * (pattern Creator GRASP).
     *
     * @param codiceCorso il codice del corso
     * @param anno        l'anno accademico (1-based)
     * @param materia     la materia da associare
     */
    public void associaMateriaACorso(String codiceCorso, int anno, Materia materia) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaById(codiceCorso);
        // Delega al CorsoDiLaurea (Creator) che valida anno e stato finalizzazione
        corso.aggiungiMateriaAdAnno(anno, materia);
    }

    /**
     * Finalizza un Corso di Laurea: dopo questa operazione il corso diventa
     * immutabile e visibile per l'immatricolazione.
     *
     * @param codiceCorso il codice del corso da finalizzare
     */
    public void finalizzaCorso(String codiceCorso) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaById(codiceCorso);
        corso.finalizza();
    }

    // =========================================================================
    // RICERCA
    // =========================================================================

    /**
     * Cerca un corso di laurea per denominazione esatta.
     *
     * @param nome denominazione del corso
     * @return istanza di CorsoDiLaurea
     * @throws CorsoDiLaureaNonTrovatoException se il corso non esiste
     */
    public CorsoDiLaurea trovaCorsoDiLaureaByNome(String nome) {
        if (nome != null) {
            for (CorsoDiLaurea corso : corsiDiLaurea) {
                if (corso.getNome().equalsIgnoreCase(nome)) {
                    return corso;
                }
            }
        }
        throw new CorsoDiLaureaNonTrovatoException("Corso di laurea non trovato: " + nome);
    }

    /**
     * Cerca un corso di laurea per codice identificativo.
     *
     * @param id codice corso
     * @return istanza di CorsoDiLaurea
     * @throws CorsoDiLaureaNonTrovatoException se il corso non esiste
     */
    public CorsoDiLaurea trovaCorsoDiLaureaById(String id) {
        if (id != null) {
            for (CorsoDiLaurea corso : corsiDiLaurea) {
                if (corso.getId().equalsIgnoreCase(id)) {
                    return corso;
                }
            }
        }
        throw new CorsoDiLaureaNonTrovatoException("Nessun corso di laurea trovato con ID: " + id);
    }

    /**
     * Restituisce solo i corsi attivi, finalizzati e non obsoleti.
     * Un corso non finalizzato NON è visibile per l'immatricolazione.
     *
     * @return lista corsi attivi e finalizzati
     */
    public List<CorsoDiLaurea> getCorsiAttivi() {
        return corsiDiLaurea.stream()
                .filter(c -> !c.isObsoleto() && c.isFinalizzato())
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutti i corsi (anche obsoleti e non finalizzati).
     *
     * @return lista completa corsi
     */
    public List<CorsoDiLaurea> getTuttiCorsi() {
        return corsiDiLaurea;
    }

    /**
     * Aggiunge un corso creato manualmente (retrocompatibilità con popolaDataBase).
     *
     * @param corso corso di laurea da aggiungere
     */
    public void addCorsoDiLaurea(CorsoDiLaurea corso) {
        corsiDiLaurea.add(corso);
    }
}
