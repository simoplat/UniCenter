package it.project;

import java.util.ArrayList;
import java.util.List;

import it.project.observer.ObserverNotifica;
import it.project.strategy.ICalcoloTasseStrategy;

/**
 * Rappresenta uno studente iscritto a UniCenter.
 * Incapsula la carriera accademica (anno di corso, tasse, piano di studi),
 * il libretto degli esami superati, la gestione dei preferiti per il materiale didattico
 * e implementa ObserverNotifica per ricevere avvisi su appelli, esiti e piano di studi.
 */
public class Studente extends Utente implements ObserverNotifica {
    private List<Notifica> notifiche;
    private Carriera carriera;
    private Libretto libretto;
    private List<String> preferitiMaterialeIds;

    /**
     * Costruttore completo per la creazione di uno studente.
     *
     * @param matricola       matricola assegnata allo studente
     * @param nome            nome dello studente
     * @param cognome         cognome dello studente
     * @param email           indirizzo email istituzionale
     * @param password        password di accesso
     * @param codiceFiscale   codice fiscale
     * @param idCorsoDiLaurea identificativo del corso di laurea a cui è iscritto
     */
    public Studente(String matricola, String nome,
            String cognome, String email,
            String password, String codiceFiscale, String idCorsoDiLaurea) {
        super(nome, cognome, email, password, codiceFiscale);
        this.setNome(nome);
        this.setCognome(cognome);
        this.setEmail(email);
        this.notifiche = new ArrayList<>();
        this.preferitiMaterialeIds = new ArrayList<>();
        this.carriera = new Carriera(matricola, idCorsoDiLaurea);
        this.libretto = new Libretto();
    }

    /**
     * Aggiunge una notifica alla lista delle notifiche dello studente.
     *
     * @param notifica notifica da aggiungere
     */
    public void aggiungiNotifica(Notifica notifica) {
        this.notifiche.add(notifica);
    }

    /**
     * Restituisce la matricola dello studente.
     *
     * @return matricola
     */
    public String getMatricola() {
        return this.carriera.getMatricola();
    }

    /**
     * Restituisce l'importo delle tasse universitarie calcolato per la carriera dello studente.
     *
     * @return importo tasse
     */
    public double getTasse() {
        return this.carriera.getTasse();
    }

    /**
     * Restituisce la lista di tutte le notifiche ricevute dallo studente.
     *
     * @return lista notifiche
     */
    public List<Notifica> getNotifiche() {
        return notifiche;
    }

    // =========================================================================
    // UC10: GESTIONE PREFERITI MATERIALE DIDATTICO (Information Expert)
    // =========================================================================

    /**
     * Restituisce una copia della lista degli identificativi dei materiali preferiti.
     *
     * @return lista di id elementi preferiti
     */
    public List<String> getPreferitiMaterialeIds() {
        return new ArrayList<>(preferitiMaterialeIds);
    }

    /**
     * Verifica se un elemento didattico è contrassegnato come preferito dallo studente.
     *
     * @param idElemento id dell'elemento
     * @return true se preferito, false altrimenti
     */
    public boolean isPreferito(String idElemento) {
        return idElemento != null && preferitiMaterialeIds.contains(idElemento);
    }

    /**
     * Attiva/disattiva lo stato di preferito per un elemento didattico.
     *
     * @param idElemento id dell'elemento didattico
     * @return true se ora è preferito, false se è stato rimosso dai preferiti
     */
    public boolean togglePreferito(String idElemento) {
        if (idElemento == null)
            return false;
        if (preferitiMaterialeIds.contains(idElemento)) {
            preferitiMaterialeIds.remove(idElemento);
            return false; // non più preferito
        } else {
            preferitiMaterialeIds.add(idElemento);
            return true; // adesso preferito
        }
    }

    /**
     * Aggiunge un elemento didattico ai preferiti se non già presente.
     *
     * @param idElemento id dell'elemento didattico
     */
    public void aggiungiPreferito(String idElemento) {
        if (idElemento != null && !preferitiMaterialeIds.contains(idElemento)) {
            preferitiMaterialeIds.add(idElemento);
        }
    }

    /**
     * Rimuove un elemento didattico dai preferiti.
     *
     * @param idElemento id dell'elemento didattico
     */
    public void rimuoviPreferito(String idElemento) {
        if (idElemento != null) {
            preferitiMaterialeIds.remove(idElemento);
        }
    }

    @Override
    public void riceviNotifica(Notifica notifica) {
        notifiche.add(notifica);
    }

    @Override
    public String toString() {
        return "Studente [matricola=" + this.carriera.getMatricola() + ", nome=" + getNome() + ", cognome="
                + getCognome()
                + ", codiceFiscale=" + getCodiceFiscale() + ", notifiche=" + notifiche + "]";
    }

    /**
     * Restituisce il piano di studi dello studente.
     *
     * @return piano di studi
     */
    public PianoDiStudi getPianoDiStudi() {
        return this.carriera.getPianoDiStudi();
    }

    /**
     * Restituisce l'identificativo del corso di laurea a cui lo studente appartiene.
     *
     * @return id corso di laurea
     */
    public String getIdCorsoDiLaurea() {
        return this.carriera.getIdCorsoDiLaurea();
    }

    /**
     * Imposta il piano di studi associato alla carriera dello studente.
     *
     * @param pianoDiStudi nuovo piano di studi
     */
    public void setPianoDiStudi(PianoDiStudi pianoDiStudi) {
        this.carriera.setPianoDiStudi(pianoDiStudi);
    }

    /**
     * Verifica se le tasse dello studente risultano saldate.
     *
     * @return true se le tasse sono pagate, false altrimenti
     */
    public boolean isTassePagate() {
        return this.carriera.isTassePagate();
    }

    /**
     * Imposta lo stato di pagamento delle tasse.
     *
     * @param tassePagate true se pagate, false altrimenti
     */
    public void setTassePagate(boolean tassePagate) {
        this.carriera.setTassePagate(tassePagate);
    }

    /**
     * Restituisce il libretto universitario dello studente.
     *
     * @return libretto universitario
     */
    public Libretto getLibretto() {
        return this.libretto;
    }

    /**
     * Restituisce l'oggetto Carriera dello studente.
     *
     * @return carriera accademica
     */
    public Carriera getCarriera() {
        return this.carriera;
    }

    /**
     * Restituisce l'anno di corso attuale.
     *
     * @return anno di corso
     */
    public int getAnnoCorrente() {
        return this.carriera.getAnnoCorrente();
    }

    /**
     * Restituisce l'anno solare di immatricolazione.
     *
     * @return anno di immatricolazione
     */
    public int getAnnoImmatricolazione() {
        return this.carriera.getAnnoImmatricolazione();
    }

    /**
     * Imposta l'anno di immatricolazione.
     *
     * @param annoImmatricolazione anno di immatricolazione
     */
    public void setAnnoImmatricolazione(int annoImmatricolazione) {
        this.carriera.setAnnoImmatricolazione(annoImmatricolazione);
    }

    /**
     * Restituisce l'anno solare dell'ultimo rinnovo effettuato.
     *
     * @return anno ultimo rinnovo
     */
    public int getAnnoUltimoRinnovo() {
        return this.carriera.getAnnoUltimoRinnovo();
    }

    /**
     * Imposta l'anno solare dell'ultimo rinnovo.
     *
     * @param annoUltimoRinnovo anno solare ultimo rinnovo
     */
    public void setAnnoUltimoRinnovo(int annoUltimoRinnovo) {
        this.carriera.setAnnoUltimoRinnovo(annoUltimoRinnovo);
    }

    /**
     * Verifica se lo studente è attualmente fuori corso.
     *
     * @return true se fuori corso, false altrimenti
     */
    public boolean isFuoriCorso() {
        return this.carriera.isFuoriCorso();
    }

    /**
     * Verifica se il rinnovo è stato effettuato per l'anno solare corrente.
     *
     * @return true se già rinnovato per l'anno corrente
     */
    public boolean isRinnovoEffettuatoPerAnnoCorrente() {
        return this.carriera.isRinnovoEffettuatoPerAnnoCorrente();
    }

    /**
     * Imposta se il rinnovo per l'anno corrente è stato effettuato.
     *
     * @param rinnovoEffettuato true se effettuato
     */
    public void setRinnovoEffettuatoPerAnnoCorrente(boolean rinnovoEffettuato) {
        this.carriera.setRinnovoEffettuatoPerAnnoCorrente(rinnovoEffettuato);
    }

    /**
     * Calcola le tasse universitarie per la carriera dello studente.
     *
     * @param strategy  strategia di calcolo
     * @param tassaBase importo base tasse
     */
    public void calcolaTasse(ICalcoloTasseStrategy strategy, double tassaBase) {
        this.carriera.calcolaImportoTasse(strategy, tassaBase);
    }

    /**
     * Rinnova l'iscrizione dello studente per l'anno successivo e aggiunge la notifica di riepilogo.
     *
     * @param anniDurataCorso durata legale del corso di laurea
     * @param strategy        strategia di calcolo tasse
     * @param tassaBase       tassa base di rinnovo
     */
    public void rinnovaIscrizione(int anniDurataCorso, ICalcoloTasseStrategy strategy, double tassaBase) {
        this.carriera.eseguiRinnovo(anniDurataCorso, strategy, tassaBase);
        String statoCorso = this.carriera.isFuoriCorso() ? "Fuori Corso" : "In Corso";
        this.aggiungiNotifica(new Notifica(
                "Rinnovo Iscrizione",
                "Iscrizione rinnovata con successo per l'anno " + this.carriera.getAnnoCorrente() + " (" + statoCorso
                        + "). Importo tasse da saldare: " + String.format("%.2f EUR", this.carriera.getTasse()) + ".",
                it.project.database.ClockProvider.nowLocalDateTime()));
    }

}