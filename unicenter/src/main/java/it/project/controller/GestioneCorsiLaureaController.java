package it.project.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.project.CorsoDiLaurea;
import it.project.Materia;
import it.project.factory.CorsoDiLaureaFactory;

/**
 * Controller (GRASP / Facade Controller) per UC4/UC5 - Gestione Corsi di Laurea e Materie.
 * Coordina le operazioni dell'amministratore relative ai percorsi universitari:
 * creazione, aggiornamento, obsolescenza, finalizzazione e ricerca dei Corsi di Laurea.
 */
public class GestioneCorsiLaureaController {
    private final List<CorsoDiLaurea> corsiDiLaurea;

    public GestioneCorsiLaureaController() {
        this.corsiDiLaurea = new ArrayList<>();
    }

    // =========================================================================
    // OPERAZIONI CRUD (UC4)
    // =========================================================================

    /**
     * Crea un nuovo Corso di Laurea tramite la Factory e lo aggiunge al sistema.
     * La Factory applica le validazioni e genera il codice univoco (Regola di Dominio 3).
     * Il corso viene creato come "non finalizzato": non ha materie e non è visibile
     * per l'immatricolazione.
     *
     * @return il CorsoDiLaurea appena creato
     */
    public CorsoDiLaurea creaCorsoDiLaurea(String nome, String tipologia, int anniAccademici) {
        // Controlla duplicati per nome
        CorsoDiLaurea esistente = trovaCorsoDiLaureaByNome(nome);
        if (esistente != null) {
            throw new IllegalArgumentException(
                "Esiste già un corso di laurea con il nome '" + nome + "' (codice: " + esistente.getId() + ").");
        }

        // Delega la creazione alla Factory (validazione + generazione codice)
        CorsoDiLaurea nuovoCorso = CorsoDiLaureaFactory.creaCorsoDiLaurea(nome, tipologia, anniAccademici);
        corsiDiLaurea.add(nuovoCorso);
        return nuovoCorso;
    }

    /**
     * Aggiorna i dati di un Corso di Laurea esistente.
     *
     * @param codice       codice identificativo del corso da aggiornare
     * @param nuovoNome    nuovo nome (null o vuoto per non modificare)
     * @param nuovaTipologia nuova tipologia (null o vuoto per non modificare)
     * @return true se l'aggiornamento è andato a buon fine
     */
    public boolean aggiornaCorsoDiLaurea(String codice, String nuovoNome, String nuovaTipologia) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaByCodice(codice);
        if (corso == null) {
            throw new IllegalArgumentException("Corso di laurea non trovato con codice: " + codice);
        }

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
            CorsoDiLaurea altroCorso = trovaCorsoDiLaureaByNome(nuovoNome);
            if (altroCorso != null && !altroCorso.getId().equals(codice)) {
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
     */
    public boolean rendiObsoletoCorsoDiLaurea(String codice) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaByCodice(codice);
        if (corso == null) {
            throw new IllegalArgumentException("Corso di laurea non trovato con codice: " + codice);
        }

        if (corso.isObsoleto()) {
            throw new IllegalStateException("Il corso '" + corso.getNome() + "' è già obsoleto.");
        }

        corso.rendiObsoleto();
        return true;
    }

    /**
     * Elimina definitivamente un Corso di Laurea (se non ancora finalizzato o se già reso obsoleto).
     * I corsi attivi e finalizzati devono essere prima resi obsoleti per sicurezza.
     *
     * @param codice codice identificativo del corso da eliminare
     * @return true se eliminato con successo
     */
    public boolean eliminaCorsoDiLaurea(String codice) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaByCodice(codice);
        if (corso == null) {
            throw new IllegalArgumentException("Corso di laurea non trovato con codice: " + codice);
        }

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
     * Restituisce i corsi creati ma non ancora finalizzati (senza materie associate).
     * Questi sono i corsi che l'amministratore può ancora configurare.
     */
    public List<CorsoDiLaurea> getCorsiNonFinalizzati() {
        return corsiDiLaurea.stream()
                .filter(c -> !c.isFinalizzato() && !c.isObsoleto())
                .collect(Collectors.toList());
    }

    /**
     * Associa una materia a un anno specifico di un Corso di Laurea.
     * Delega al CorsoDiLaurea la validazione dell'anno e dello stato di finalizzazione
     * (pattern Creator GRASP).
     *
     * @param codiceCorso il codice del corso
     * @param anno        l'anno accademico (1-based)
     * @param materia     la materia da associare
     */
    public void associaMateriaACorso(String codiceCorso, int anno, Materia materia) {
        CorsoDiLaurea corso = trovaCorsoDiLaureaByCodice(codiceCorso);
        if (corso == null) {
            throw new IllegalArgumentException("Corso di laurea non trovato con codice: " + codiceCorso);
        }
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
        CorsoDiLaurea corso = trovaCorsoDiLaureaByCodice(codiceCorso);
        if (corso == null) {
            throw new IllegalArgumentException("Corso di laurea non trovato con codice: " + codiceCorso);
        }
        corso.finalizza();
    }

    // =========================================================================
    // RICERCA
    // =========================================================================

    public CorsoDiLaurea trovaCorsoDiLaureaByNome(String nome) {
        for (CorsoDiLaurea corso : corsiDiLaurea) {
            if (corso.getNome().equalsIgnoreCase(nome)) {
                return corso;
            }
        }
        return null;
    }

    public CorsoDiLaurea trovaCorsoDiLaureaByCodice(String codice) {
        for (CorsoDiLaurea corso : corsiDiLaurea) {
            if (corso.getId().equalsIgnoreCase(codice)) {
                return corso;
            }
        }
        return null;
    }

    /**
     * Restituisce solo i corsi attivi, finalizzati e non obsoleti.
     * Un corso non finalizzato NON è visibile per l'immatricolazione.
     */
    public List<CorsoDiLaurea> getCorsiAttivi() {
        return corsiDiLaurea.stream()
                .filter(c -> !c.isObsoleto() && c.isFinalizzato())
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutti i corsi (anche obsoleti e non finalizzati).
     */
    public List<CorsoDiLaurea> getTuttiCorsi() {
        return corsiDiLaurea;
    }

    /**
     * Aggiunge un corso creato manualmente (retrocompatibilità con popolaDataBase).
     */
    public void addCorsoDiLaurea(CorsoDiLaurea corso) {
        corsiDiLaurea.add(corso);
    }
}
