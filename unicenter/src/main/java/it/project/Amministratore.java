package it.project;

/**
 * Ruolo Amministratore del sistema UniCenter.
 * L'amministratore gestisce i Corsi di Laurea (UC4), le materie e l'approvazione dei piani di studio.
 */
public class Amministratore extends Utente {

    private String idAmministratore;

    /**
     * Costruttore completo per l'amministratore.
     *
     * @param idAmministratore identificativo univoco dell'amministratore
     * @param nome             nome dell'amministratore
     * @param cognome          cognome dell'amministratore
     * @param email            indirizzo email istituzionale
     * @param password         password di accesso
     * @param codiceFiscale    codice fiscale
     */
    public Amministratore(String idAmministratore, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(nome, cognome, email, password, codiceFiscale);
        this.idAmministratore = idAmministratore;
    }

    /**
     * Restituisce l'identificativo dell'amministratore.
     *
     * @return id dell'amministratore
     */
    public String getIdAmministratore() {
        return idAmministratore;
    }

    /**
     * Imposta l'identificativo dell'amministratore.
     *
     * @param idAmministratore nuovo id per l'amministratore
     */
    public void setIdAmministratore(String idAmministratore) {
        this.idAmministratore = idAmministratore;
    }

    @Override
    public String toString() {
        return "Amministratore [id=" + idAmministratore + ", nome=" + getNome() + ", cognome=" + getCognome() + "]";
    }
}
