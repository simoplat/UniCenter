package it.project;

import it.project.strategy.ICalcoloTasseStrategy;

public class Carriera {
    private String matricola;
    private String idCorsoDiLaurea;
    private boolean tassePagate;
    private double totaleTasse;
    private PianoDiStudi pianoStudi;
    private boolean isFuoriCorso;
    private int annoCorrente = 1;
    private boolean rinnovoEffettuatoPerAnnoCorrente = false;
    private int annoImmatricolazione;
    private int annoUltimoRinnovo;

    // Costruttore per creare una nuova carriera
    public Carriera(String matricola, String idCorsoDiLaurea) {
        this.matricola = matricola;
        this.idCorsoDiLaurea = idCorsoDiLaurea;
        this.pianoStudi = new PianoDiStudi();
        this.annoCorrente = 1;
        this.isFuoriCorso = false;
        this.rinnovoEffettuatoPerAnnoCorrente = false;
        this.annoImmatricolazione = it.project.database.ClockProvider.nowLocalDate().getYear();
        this.annoUltimoRinnovo = 0;
    }

    public Carriera(String matricola, String idCorsoDiLaurea, int annoImmatricolazione) {
        this(matricola, idCorsoDiLaurea);
        this.annoImmatricolazione = annoImmatricolazione;
        this.annoUltimoRinnovo = 0;
    }

    @Override
    public String toString() {
        return "Carriera [matricola=" + matricola + ", idCorsoDiLaurea=" + idCorsoDiLaurea + ", pianoStudi="
                + pianoStudi + ", annoCorrente=" + annoCorrente + ", fuoriCorso=" + isFuoriCorso
                + ", annoImmatricolazione=" + annoImmatricolazione + ", annoUltimoRinnovo=" + annoUltimoRinnovo + "]";
    }

    public String getMatricola() {
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

    public double getTasse() {
        return totaleTasse;
    }

    public boolean isTassePagate() {
        return this.tassePagate;
    }

    public int getAnnoCorrente() {
        return this.annoCorrente;
    }

    public void setAnnoCorrente(int annoCorrente) {
        this.annoCorrente = annoCorrente;
    }

    public int getAnnoImmatricolazione() {
        return this.annoImmatricolazione;
    }

    public void setAnnoImmatricolazione(int annoImmatricolazione) {
        this.annoImmatricolazione = annoImmatricolazione;
    }

    public void incrementaAnnoCorrente() {
        this.annoCorrente++;
    }

    public boolean isFuoriCorso() {
        return this.isFuoriCorso;
    }

    public void setFuoriCorso(boolean isFuoriCorso) {
        this.isFuoriCorso = isFuoriCorso;
    }

    public int getAnnoUltimoRinnovo() {
        return this.annoUltimoRinnovo;
    }

    public void setAnnoUltimoRinnovo(int annoUltimoRinnovo) {
        this.annoUltimoRinnovo = annoUltimoRinnovo;
    }

    public boolean isRinnovoEffettuatoPerAnnoCorrente() {
        int currentCalendarYear = it.project.database.ClockProvider.nowLocalDate().getYear();
        return this.annoUltimoRinnovo > 0 && this.annoUltimoRinnovo >= currentCalendarYear;
    }

    public void setRinnovoEffettuatoPerAnnoCorrente(boolean rinnovoEffettuato) {
        this.rinnovoEffettuatoPerAnnoCorrente = rinnovoEffettuato;
        if (rinnovoEffettuato) {
            this.annoUltimoRinnovo = it.project.database.ClockProvider.nowLocalDate().getYear();
        } else {
            this.annoUltimoRinnovo = 0;
        }
    }

    public void eseguiRinnovo(int anniDurataCorso, ICalcoloTasseStrategy strategy, double tassaBaseCorso) {
        incrementaAnnoCorrente();
        if (this.annoCorrente > anniDurataCorso) {
            this.isFuoriCorso = true;
        }
        calcolaImportoTasse(strategy, tassaBaseCorso);
        this.tassePagate = false;
        this.annoUltimoRinnovo = it.project.database.ClockProvider.nowLocalDate().getYear();
        this.rinnovoEffettuatoPerAnnoCorrente = true;
    }

    public void setTassePagate(boolean tassePagate) {
        this.tassePagate = tassePagate;
    }

}
