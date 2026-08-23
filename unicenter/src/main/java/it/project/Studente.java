package it.project;

import java.util.ArrayList;
import java.util.List;

import it.project.observer.ObserverNotifica;
import it.project.strategy.ICalcoloTasseStrategy;

public class Studente extends Utente implements ObserverNotifica {
    private List<Notifica> notifiche;
    private Carriera carriera;
    private Libretto libretto;
    private List<String> preferitiMaterialeIds;

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

    // =========================================================================
    // UC10: GESTIONE PREFERITI MATERIALE DIDATTICO (Information Expert)
    // =========================================================================

    public List<String> getPreferitiMaterialeIds() {
        return new ArrayList<>(preferitiMaterialeIds);
    }

    public boolean isPreferito(String idElemento) {
        return idElemento != null && preferitiMaterialeIds.contains(idElemento);
    }

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

    public void aggiungiPreferito(String idElemento) {
        if (idElemento != null && !preferitiMaterialeIds.contains(idElemento)) {
            preferitiMaterialeIds.add(idElemento);
        }
    }

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

    public Libretto getLibretto() {
        return this.libretto;
    }

    public Carriera getCarriera() {
        return this.carriera;
    }

    public int getAnnoCorrente() {
        return this.carriera.getAnnoCorrente();
    }

    public int getAnnoImmatricolazione() {
        return this.carriera.getAnnoImmatricolazione();
    }

    public void setAnnoImmatricolazione(int annoImmatricolazione) {
        this.carriera.setAnnoImmatricolazione(annoImmatricolazione);
    }

    public int getAnnoUltimoRinnovo() {
        return this.carriera.getAnnoUltimoRinnovo();
    }

    public void setAnnoUltimoRinnovo(int annoUltimoRinnovo) {
        this.carriera.setAnnoUltimoRinnovo(annoUltimoRinnovo);
    }

    public boolean isFuoriCorso() {
        return this.carriera.isFuoriCorso();
    }

    public boolean isRinnovoEffettuatoPerAnnoCorrente() {
        return this.carriera.isRinnovoEffettuatoPerAnnoCorrente();
    }

    public void setRinnovoEffettuatoPerAnnoCorrente(boolean rinnovoEffettuato) {
        this.carriera.setRinnovoEffettuatoPerAnnoCorrente(rinnovoEffettuato);
    }

    public void calcolaTasse(ICalcoloTasseStrategy strategy, double tassaBase) {
        this.carriera.calcolaImportoTasse(strategy, tassaBase);
    }

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