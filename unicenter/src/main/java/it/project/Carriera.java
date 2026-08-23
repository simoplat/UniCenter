package it.project;

import it.project.strategy.ICalcoloTasseStrategy;

/**
 * Rappresenta la carriera accademica di uno studente all'interno di un corso di laurea.
 * Gestisce l'anno di corso, l'anno di immatricolazione, lo stato fuori corso,
 * lo storico dei rinnovi annuali, il calcolo delle tasse e il piano di studi associato.
 */
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

    /**
     * Costruttore per creare una nuova carriera al primo anno accademico.
     *
     * @param matricola       matricola dello studente
     * @param idCorsoDiLaurea identificativo del corso di laurea
     */
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

    /**
     * Costruttore con anno di immatricolazione esplicito.
     *
     * @param matricola            matricola dello studente
     * @param idCorsoDiLaurea      identificativo del corso di laurea
     * @param annoImmatricolazione anno solare di immatricolazione
     */
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

    /**
     * Restituisce la matricola dello studente.
     *
     * @return matricola
     */
    public String getMatricola() {
        return this.matricola;
    }

    /**
     * Restituisce il piano di studi associato alla carriera.
     *
     * @return piano di studi
     */
    public PianoDiStudi getPianoDiStudi() {
        return this.pianoStudi;
    }

    /**
     * Restituisce l'identificativo del corso di laurea.
     *
     * @return id corso di laurea
     */
    public String getIdCorsoDiLaurea() {
        return this.idCorsoDiLaurea;
    }

    /**
     * Imposta il piano di studi per la carriera.
     *
     * @param pianoDiStudi il piano di studi da associare
     */
    public void setPianoDiStudi(PianoDiStudi pianoDiStudi) {
        this.pianoStudi = pianoDiStudi;
    }

    /**
     * Calcola l'importo totale delle tasse universitarie delegando alla strategia di calcolo.
     *
     * @param strategy       la strategia di calcolo delle tasse
     * @param tassaBaseCorso l'importo base delle tasse per il corso
     */
    public void calcolaImportoTasse(ICalcoloTasseStrategy strategy, double tassaBaseCorso) {
        this.totaleTasse = strategy.calcolaTasse(tassaBaseCorso, isFuoriCorso);
    }

    /**
     * Restituisce l'importo totale delle tasse da pagare o pagate.
     *
     * @return importo tasse
     */
    public double getTasse() {
        return totaleTasse;
    }

    /**
     * Indica se le tasse risultano pagate.
     *
     * @return true se le tasse sono saldate, false altrimenti
     */
    public boolean isTassePagate() {
        return this.tassePagate;
    }

    /**
     * Restituisce l'anno di corso attuale dello studente.
     *
     * @return anno di corso (1, 2, 3...)
     */
    public int getAnnoCorrente() {
        return this.annoCorrente;
    }

    /**
     * Imposta l'anno di corso attuale.
     *
     * @param annoCorrente nuovo anno di corso
     */
    public void setAnnoCorrente(int annoCorrente) {
        this.annoCorrente = annoCorrente;
    }

    /**
     * Restituisce l'anno solare di immatricolazione.
     *
     * @return anno di immatricolazione
     */
    public int getAnnoImmatricolazione() {
        return this.annoImmatricolazione;
    }

    /**
     * Imposta l'anno solare di immatricolazione.
     *
     * @param annoImmatricolazione anno di immatricolazione
     */
    public void setAnnoImmatricolazione(int annoImmatricolazione) {
        this.annoImmatricolazione = annoImmatricolazione;
    }

    /**
     * Incrementa di uno l'anno di corso dello studente.
     */
    public void incrementaAnnoCorrente() {
        this.annoCorrente++;
    }

    /**
     * Verifica se lo studente è fuori corso.
     *
     * @return true se fuori corso, false altrimenti
     */
    public boolean isFuoriCorso() {
        return this.isFuoriCorso;
    }

    /**
     * Imposta lo stato fuori corso dello studente.
     *
     * @param isFuoriCorso true se lo studente è fuori corso
     */
    public void setFuoriCorso(boolean isFuoriCorso) {
        this.isFuoriCorso = isFuoriCorso;
    }

    /**
     * Restituisce l'anno solare dell'ultimo rinnovo effettuato.
     *
     * @return anno ultimo rinnovo
     */
    public int getAnnoUltimoRinnovo() {
        return this.annoUltimoRinnovo;
    }

    /**
     * Imposta l'anno solare dell'ultimo rinnovo.
     *
     * @param annoUltimoRinnovo anno solare di rinnovo
     */
    public void setAnnoUltimoRinnovo(int annoUltimoRinnovo) {
        this.annoUltimoRinnovo = annoUltimoRinnovo;
    }

    /**
     * Verifica se il rinnovo dell'iscrizione è già stato effettuato per l'anno solare corrente.
     *
     * @return true se già rinnovato per l'anno corrente, false altrimenti
     */
    public boolean isRinnovoEffettuatoPerAnnoCorrente() {
        int currentCalendarYear = it.project.database.ClockProvider.nowLocalDate().getYear();
        return this.annoUltimoRinnovo > 0 && this.annoUltimoRinnovo >= currentCalendarYear;
    }

    /**
     * Imposta il flag di avvenuto rinnovo per l'anno corrente.
     *
     * @param rinnovoEffettuato true se effettuato, false altrimenti
     */
    public void setRinnovoEffettuatoPerAnnoCorrente(boolean rinnovoEffettuato) {
        this.rinnovoEffettuatoPerAnnoCorrente = rinnovoEffettuato;
        if (rinnovoEffettuato) {
            this.annoUltimoRinnovo = it.project.database.ClockProvider.nowLocalDate().getYear();
        } else {
            this.annoUltimoRinnovo = 0;
        }
    }

    /**
     * Esegue l'avanzamento della carriera e rinnova l'iscrizione per l'anno accademico successivo.
     * Incrementa l'anno di corso, aggiorna lo stato fuori corso se superata la durata legale,
     * calcola il nuovo importo delle tasse e imposta lo stato di pagamento a non saldato.
     *
     * @param anniDurataCorso durata legale del corso di laurea in anni
     * @param strategy        strategia per il calcolo delle tasse
     * @param tassaBaseCorso  tassa base di rinnovo del corso
     */
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

    /**
     * Imposta lo stato di pagamento delle tasse.
     *
     * @param tassePagate true se le tasse sono state saldate
     */
    public void setTassePagate(boolean tassePagate) {
        this.tassePagate = tassePagate;
    }
}
