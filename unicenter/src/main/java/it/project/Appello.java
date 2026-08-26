package it.project;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.project.observer.ObserverNotifica;

/**
 * Rappresenta un appello d'esame per una determinata materia.
 * Gestisce i dettagli logistici (aula, data/ora, posti disponibili, vincoli di cognome),
 * il periodo di apertura delle iscrizioni e la lista degli studenti iscritti,
 * fungendo anche da Subject per le notifiche agli iscritti.
 */
public class Appello {
    private String codiceAppello;
    private String codiceMateria;
    private LocalDateTime dataOra;
    private String aula;
    private int postiDisponibili;
    private String vincoloLetteraCognome;
    private LocalDate termineIscrizione;
    private List<Studente> iscritti;

    /**
     * Costruttore completo per la creazione di un appello d'esame.
     *
     * @param codiceAppello         codice identificativo univoco dell'appello
     * @param codiceMateria         codice della materia associata
     * @param dataOraStr            data e ora di svolgimento dell'esame
     * @param aula                  aula in cui si terrà l'esame
     * @param postiDisponibili      numero massimo di posti disponibili per la prenotazione
     * @param vincoloLetteraCognome eventuale vincolo di lettera iniziale cognome (es. "A-L") o null/vuoto
     * @param termineIscrizione     data di scadenza per l'iscrizione all'appello
     */
    public Appello(String codiceAppello, String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili, String vincoloLetteraCognome, LocalDate termineIscrizione) {
        this.codiceAppello = codiceAppello;
        this.codiceMateria = codiceMateria;
        this.dataOra = dataOraStr;
        this.aula = aula;
        this.postiDisponibili = postiDisponibili;
        this.vincoloLetteraCognome = vincoloLetteraCognome;
        this.termineIscrizione = termineIscrizione;
        this.iscritti = new ArrayList<>();
    }

    /**
     * Aggiunge uno studente alla lista degli iscritti all'appello e decrementa i posti disponibili.
     *
     * @param studente lo studente da iscrivere
     * @throws IllegalStateException se i posti disponibili sono esauriti
     */
    public synchronized void aggiungiIscritto(Studente studente) {
        if (postiDisponibili <= 0) {
            throw new IllegalStateException("Nessun posto disponibile per l'appello " + codiceAppello);
        }
        iscritti.add(studente);
        postiDisponibili--;
    }

    /**
     * Rimuove uno studente dalla lista degli iscritti all'appello e incrementa i posti disponibili.
     *
     * @param studente lo studente da disiscrivere
     */
    public synchronized void rimuoviIscritto(Studente studente) {
        if (iscritti.remove(studente)) {
            postiDisponibili++;
        }
    }

    /**
     * Restituisce il codice dell'appello.
     *
     * @return codice appello
     */
    public String getCodiceAppello() { return codiceAppello; }

    /**
     * Restituisce il codice della materia dell'appello.
     *
     * @return codice materia
     */
    public String getCodiceMateria() { return codiceMateria; }

    /**
     * Restituisce la data e l'ora di svolgimento dell'esame.
     *
     * @return data e ora appello
     */
    public LocalDateTime getDataOra() { return dataOra; }

    /**
     * Restituisce l'aula dell'appello.
     *
     * @return aula d'esame
     */
    public String getAula() { return aula; }

    /**
     * Restituisce il numero di posti ancora disponibili.
     *
     * @return posti disponibili
     */
    public int getPostiDisponibili() { return postiDisponibili; }

    /**
     * Restituisce il vincolo di lettera del cognome.
     *
     * @return vincolo cognome (o stringa vuota/null)
     */
    public String getVincoloLetteraCognome() { return vincoloLetteraCognome; }

    /**
     * Restituisce la data limite per le iscrizioni all'appello.
     *
     * @return data termine iscrizione
     */
    public LocalDate getTermineIscrizione() { return termineIscrizione; }

    /**
     * Restituisce la lista degli studenti iscritti all'appello.
     *
     * @return lista studenti iscritti
     */
    public List<Studente> getIscritti() { return Collections.unmodifiableList(iscritti); }

    /**
     * Imposta il codice della materia associata all'appello.
     *
     * @param codiceMateria nuovo codice materia
     */
    public void setCodiceMateria(String codiceMateria) {
        this.codiceMateria = codiceMateria;
    }

    /**
     * Imposta la data e l'ora di svolgimento dell'esame.
     *
     * @param dataOra nuova data e ora
     */
    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    /**
     * Imposta l'aula dell'esame.
     *
     * @param aula nuova aula
     */
    public void setAula(String aula) {
        this.aula = aula;
    }

    /**
     * Imposta il numero di posti disponibili.
     *
     * @param postiDisponibili numero di posti
     */
    public void setPostiDisponibili(int postiDisponibili) {
        this.postiDisponibili = postiDisponibili;
    }

    /**
     * Imposta il vincolo di fascia alfabetica sul cognome.
     *
     * @param vincoloLetteraCognome vincolo alfabetico
     */
    public void setVincoloLetteraCognome(String vincoloLetteraCognome) {
        this.vincoloLetteraCognome = vincoloLetteraCognome;
    }

    /**
     * Imposta il termine ultimo per le iscrizioni.
     *
     * @param termineIscrizione data limite
     */
    public void setTermineIscrizione(LocalDate termineIscrizione) {
        this.termineIscrizione = termineIscrizione;
    }

    /**
     * Verifica se le iscrizioni all'appello sono ancora aperte rispetto alla data corrente.
     *
     * @return true se la data corrente non supera la data limite di iscrizione, false altrimenti
     */
    public boolean isIscrizioneAperta() {
        LocalDate oggi = it.project.database.ClockProvider.nowLocalDate();
        return termineIscrizione != null && !oggi.isAfter(termineIscrizione);
    }

    @Override
    public String toString() {
        return "Appello [codiceAppello=" + codiceAppello + ", codiceMateria=" + codiceMateria + ", dataOra=" + dataOra
                + ", aula=" + aula + ", vincoloLetteraCognome=" + vincoloLetteraCognome + "]";
    }

    /**
     * Invia una notifica a tutti gli studenti iscritti all'appello.
     *
     * @param notifica la notifica da inoltrare
     */
    public void notifica(Notifica notifica) {
        for (ObserverNotifica observer : iscritti) {
            observer.riceviNotifica(notifica);
        }
    }
}