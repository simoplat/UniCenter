package it.project;

/**
 * Ruolo Amministratore del sistema UniCenter.
 * L'amministratore gestisce i Corsi di Laurea (UC4).
 */
public class Amministratore extends Utente {

    private String idAmministratore;

    public Amministratore(String idAmministratore, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(nome, cognome, email, password, codiceFiscale);
        this.idAmministratore = idAmministratore;
    }

    public String getIdAmministratore() {
        return idAmministratore;
    }

    public void setIdAmministratore(String idAmministratore) {
        this.idAmministratore = idAmministratore;
    }

    @Override
    public String toString() {
        return "Amministratore [id=" + idAmministratore + ", nome=" + getNome() + ", cognome=" + getCognome() + "]";
    }
}
