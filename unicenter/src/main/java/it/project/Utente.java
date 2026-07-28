package it.project;

public abstract class Utente {
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String codiceFiscale;

    public Utente() {
    }

    // Costruttore a 5 parametri con la password e il codice fiscale
    public Utente(String nome, String cognome, String email, String password, String codiceFiscale) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.codiceFiscale = codiceFiscale;
    }

    // Getter e Setter
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }
}