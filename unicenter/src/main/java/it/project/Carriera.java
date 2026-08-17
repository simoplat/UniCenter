package it.project;

import it.project.strategy.ICalcoloTasseStrategy;

public class Carriera {
    private String matricola;
    private String idCorsoDiLaurea;
    private boolean tassePagate;
    private double totaleTasse;
    private PianoDiStudi pianoStudi;
    private boolean isFuoriCorso;
    private int annoCorrente = 0;

    // Costruttore per creare una nuova carriera
    public Carriera(String matricola, String idCorsoDiLaurea) {
        this.matricola = matricola;
        this.idCorsoDiLaurea = idCorsoDiLaurea;
        this.pianoStudi = new PianoDiStudi();
    }

    @Override
    public String toString() {
        return "Carriera [matricola=" + matricola + ", idCorsoDiLaurea=" + idCorsoDiLaurea + ", pianoStudi="
                + pianoStudi + "]";
    }

    public String getMatricola(){
        return this.matricola;
    }

    public PianoDiStudi getPianoDiStudi() {
        return this.pianoStudi;
    }

    public String getIdCorsoDiLaurea() {
        return this.idCorsoDiLaurea;
    }

    public void setPianoDiStudi(PianoDiStudi pianoDiStudi) {
        this.pianoStudi = pianoDiStudi;
    }

     public void calcolaImportoTasse(ICalcoloTasseStrategy strategy, double tassaBaseCorso) {
        this.totaleTasse = strategy.calcolaTasse(tassaBaseCorso, isFuoriCorso);
    }

    public double getTasse(){
        return totaleTasse;
    }

    public boolean isTassePagate(){
        return this.tassePagate;
    }

    public void setTassePagate(){
        this.tassePagate = true;
    }

    public int getAnnoCorrente() {
        return this.annoCorrente;
    }

    public void incrementaAnnoCorrente() {
        this.annoCorrente++;
    }

    public void setTassePagate(boolean tassePagate) {
    this.tassePagate = tassePagate;
}

}
