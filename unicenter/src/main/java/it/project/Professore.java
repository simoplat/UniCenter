package it.project;

public class Professore extends Utente {

    private String idProfessore;


    // 1. Costruttore principale a 6 parametri (con ID in prima posizione come String)
    public Professore(String idProfessore, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(nome, cognome, email, password, codiceFiscale);
        this.idProfessore = idProfessore;
    }

    // 2. Overload: accetta l'ID come numero intero (es. 1 anziché "1") e lo converte in String
    public Professore(int idProfessore, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(nome, cognome, email, password, codiceFiscale);
        this.idProfessore = String.valueOf(idProfessore);
    }

    // 3. Overload a 4 parametri per retrocompatibilità (se creato senza ID)
    public Professore(String nome, String cognome, String email, String password, String codiceFiscale) {
        super(nome, cognome, email, password, codiceFiscale);
        this.idProfessore = null;
    }

    // =========================================================================
    // GETTER & SETTER (Overloading del Setter)
    // =========================================================================

    public String getIdProfessore() {
        return idProfessore;
    }

    // Setter principale per String
    public void setIdProfessore(String idProfessore) {
        this.idProfessore = idProfessore;
    }

    // Overload del Setter: converte automaticamente da int a String
    public void setIdProfessore(int idProfessore) {
        this.idProfessore = String.valueOf(idProfessore);
    }

}