package it.project;

import java.util.ArrayList;
import java.util.List;

public class Professore extends Utente {

    private String idProfessore;
    private List<Notifica> notifiche;


    // 1. Costruttore principale a 6 parametri (con ID in prima posizione come String)
    public Professore(String idProfessore, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(nome, cognome, email, password, codiceFiscale);
        this.idProfessore = idProfessore;
        this.notifiche = new ArrayList<>();
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

    // =========================================================================
    // NOTIFICHE (Observer Pattern)
    // =========================================================================

    public void aggiungiNotifica(Notifica notifica) {
        this.notifiche.add(notifica);
    }

    public List<Notifica> getNotifiche() {
        return notifiche;
    }

}