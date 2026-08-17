package it.project.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.project.CorsoDiLaurea;
import it.project.factory.CorsoDiLaureaFactory;

/**
 * Controller (GRASP / Facade Controller) per UC4 - Gestione Corsi di Laurea.
 * Coordina le operazioni dell'amministratore relative ai percorsi universitari:
 * creazione, aggiornamento, obsolescenza e ricerca dei Corsi di Laurea.
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
     * Restituisce solo i corsi attivi (non obsoleti).
     */
    public List<CorsoDiLaurea> getCorsiAttivi() {
        return corsiDiLaurea.stream()
                .filter(c -> !c.isObsoleto())
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutti i corsi (anche obsoleti).
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
