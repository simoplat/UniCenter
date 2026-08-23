package it.project;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un docente/professore nel sistema UniCenter.
 * Può essere titolare di materie, creare appelli, verbalizzare voti, caricare materiale didattico
 * e inviare comunicazioni di corso agli studenti iscritti.
 */
public class Professore extends Utente {

    private String idProfessore;
    private List<Notifica> notifiche;

    /**
     * Costruttore completo per il docente.
     *
     * @param idProfessore  identificativo univoco del professore
     * @param nome          nome del docente
     * @param cognome       cognome del docente
     * @param email         indirizzo email istituzionale
     * @param password      password di accesso
     * @param codiceFiscale codice fiscale
     */
    public Professore(String idProfessore, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(nome, cognome, email, password, codiceFiscale);
        this.idProfessore = idProfessore;
        this.notifiche = new ArrayList<>();
    }

    // =========================================================================
    // GETTER & SETTER (Overloading del Setter)
    // =========================================================================

    /**
     * Restituisce l'identificativo univoco del professore.
     *
     * @return id professore
     */
    public String getIdProfessore() {
        return idProfessore;
    }

    /**
     * Imposta l'identificativo del professore da stringa.
     *
     * @param idProfessore nuovo id docente
     */
    public void setIdProfessore(String idProfessore) {
        this.idProfessore = idProfessore;
    }

    /**
     * Overload del Setter: converte automaticamente da int a String.
     *
     * @param idProfessore id numerico del docente
     */
    public void setIdProfessore(int idProfessore) {
        this.idProfessore = String.valueOf(idProfessore);
    }

    // =========================================================================
    // NOTIFICHE (Observer Pattern)
    // =========================================================================

    /**
     * Aggiunge una notifica alla lista dei messaggi ricevuti dal docente.
     *
     * @param notifica la notifica ricevuta
     */
    public void aggiungiNotifica(Notifica notifica) {
        this.notifiche.add(notifica);
    }

    /**
     * Restituisce la lista di notifiche ricevute dal docente.
     *
     * @return lista notifiche
     */
    public List<Notifica> getNotifiche() {
        return notifiche;
    }
}