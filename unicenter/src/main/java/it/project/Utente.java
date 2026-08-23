package it.project;

/**
 * Classe astratta base che rappresenta un utente generico del sistema UniCenter.
 * Contiene i dati anagrafici e le credenziali di autenticazione condivise da studenti,
 * docenti e amministratori.
 */
public abstract class Utente {
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String codiceFiscale;

    /**
     * Costruttore di default.
     */
    public Utente() {
    }

    /**
     * Costruttore completo con tutti i dati anagrafici e credenziali.
     *
     * @param nome          nome dell'utente
     * @param cognome       cognome dell'utente
     * @param email         indirizzo email
     * @param password      password di accesso
     * @param codiceFiscale codice fiscale
     */
    public Utente(String nome, String cognome, String email, String password, String codiceFiscale) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.codiceFiscale = codiceFiscale;
    }

    /**
     * Restituisce il nome dell'utente.
     *
     * @return nome
     */
    public String getNome() { return nome; }

    /**
     * Imposta il nome dell'utente.
     *
     * @param nome nuovo nome
     */
    public void setNome(String nome) { this.nome = nome; }

    /**
     * Restituisce il cognome dell'utente.
     *
     * @return cognome
     */
    public String getCognome() { return cognome; }

    /**
     * Imposta il cognome dell'utente.
     *
     * @param cognome nuovo cognome
     */
    public void setCognome(String cognome) { this.cognome = cognome; }

    /**
     * Restituisce l'email dell'utente.
     *
     * @return email
     */
    public String getEmail() { return email; }

    /**
     * Imposta l'email dell'utente.
     *
     * @param email nuova email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Restituisce la password dell'utente.
     *
     * @return password
     */
    public String getPassword() { return password; }

    /**
     * Imposta la password dell'utente.
     *
     * @param password nuova password
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Restituisce il codice fiscale dell'utente.
     *
     * @return codice fiscale
     */
    public String getCodiceFiscale() { return codiceFiscale; }

    /**
     * Imposta il codice fiscale dell'utente.
     *
     * @param codiceFiscale nuovo codice fiscale
     */
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }
}