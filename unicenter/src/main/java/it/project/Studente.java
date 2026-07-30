package it.project;

import java.util.ArrayList;
import java.util.List;

import it.project.strategy.ICalcoloTasseStrategy;

public class Studente extends Utente implements ObserverAppello{
    private String matricola;
    private String corsoDiLaurea;
    private boolean tassePagate;
    private double totaleTasse;
    private PianoDiStudi pianoStudi;
    private List<Notifica> notifiche;


    public Studente(String matricola, String nome, 
                    String cognome, String email, 
                    String password, String codiceFiscale, 
                    String corsoDiLaurea) {
        super(nome, cognome, email, password, codiceFiscale);
        this.setNome(nome);
        this.setCognome(cognome);
        this.setEmail(email);        
        this.matricola = matricola;
        this.corsoDiLaurea = corsoDiLaurea;
        this.tassePagate = false;
        this.pianoStudi = new PianoDiStudi();
        this.notifiche = new ArrayList<>();
    }

    public void calcolaImportoTasse(ICalcoloTasseStrategy strategy, double tassaBaseCorso, boolean isFuoriCorso) {
        this.totaleTasse = strategy.calcolaTasse(tassaBaseCorso, isFuoriCorso);
    }

    public PianoDiStudi getPianoStudi() {
        return pianoStudi;
    }

    public void setPianoStudi(PianoDiStudi pianoStudi) {
        this.pianoStudi = pianoStudi;
    }

    public void aggiungiNotifica(Notifica notifica) {
        this.notifiche.add(notifica);
    }

    public String getMatricola() {
        return matricola;
    }

    public String getCorsoDiLaurea() {
        return corsoDiLaurea;
    }

    public boolean isTassePagate() {
        return tassePagate;
    }

    public void setTassePagate(boolean tassePagate) {
        this.tassePagate = tassePagate;
    }

    public double getTotaleTasse() {
        return totaleTasse;
    }

    public List<Notifica> getNotifiche() {
        return notifiche;
    }

    @Override
    public void riceviNotifica(Notifica notifica) {
        notifiche.add(notifica);
    }

    @Override
    public String toString() {
        return "Studente [matricola=" + matricola + ", corsoDiLaurea=" + corsoDiLaurea + ", tassePagate=" + tassePagate
                + ", totaleTasse=" + totaleTasse + ", pianoStudi=" + pianoStudi + ", notifiche=" + notifiche + "]";
    }

    

}