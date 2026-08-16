package it.project;

import java.util.ArrayList;
import java.util.List;

import it.project.strategy.ICalcoloTasseStrategy;

public class Studente extends Utente implements ObserverAppello {
    private List<Notifica> notifiche;
    private Carriera carriera;

    public Studente(String matricola, String nome,
            String cognome, String email,
            String password, String codiceFiscale, String idCorsoDiLaurea) {
        super(nome, cognome, email, password, codiceFiscale);
        this.setNome(nome);
        this.setCognome(cognome);
        this.setEmail(email);
        this.notifiche = new ArrayList<>();
        this.carriera = new Carriera(matricola, idCorsoDiLaurea);
        ;
    }

    public void aggiungiNotifica(Notifica notifica) {
        this.notifiche.add(notifica);
    }

    public String getMatricola() {
        return this.carriera.getMatricola();
    }

    public double getTasse() {
        return this.carriera.getTasse();
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
        return "Studente [matricola=" + this.carriera.getMatricola() + ", nome=" + getNome() + ", cognome=" + getCognome()
                + ", codiceFiscale=" + getCodiceFiscale() + ", notifiche=" + notifiche + "]";
    }

    public PianoDiStudi getPianoDiStudi() {
        return this.carriera.getPianoDiStudi();
    }

    public String getIdCorsoDiLaurea() {
        return this.carriera.getIdCorsoDiLaurea();
    }

    public void setPianoDiStudi(PianoDiStudi pianoDiStudi) {
        this.carriera.setPianoDiStudi(pianoDiStudi);
    }

    public boolean isTassePagate() {
        return this.carriera.isTassePagate();
    }

    public void setTassePagate(boolean tassePagate) {
        this.carriera.setTassePagate(tassePagate);
    }

    public void calcolaTasse(ICalcoloTasseStrategy strategy, double tassaBase) {
        this.carriera.calcolaImportoTasse(strategy, tassaBase);
    }

}