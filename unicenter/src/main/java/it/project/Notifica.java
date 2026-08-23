package it.project;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Rappresenta un messaggio di notifica inviato a utenti (studenti o docenti) del sistema UniCenter.
 * Contiene l'oggetto del messaggio, il corpo del testo e il timestamp di invio.
 */
public class Notifica {
    private String oggetto;
    private String messaggio;
    private LocalDateTime dataOra;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    /**
     * Costruttore completo per la creazione di una notifica.
     *
     * @param oggetto   titolo/oggetto della notifica
     * @param messaggio contenuto testuale del messaggio
     * @param dataOra   data e ora di invio
     */
    public Notifica(String oggetto, String messaggio, LocalDateTime dataOra) {
        this.oggetto = oggetto;
        this.messaggio = messaggio;
        this.dataOra = dataOra;
    }

    /**
     * Restituisce l'oggetto della notifica.
     *
     * @return oggetto
     */
    public String getOggetto() {
        return oggetto;
    }

    /**
     * Imposta l'oggetto della notifica.
     *
     * @param oggetto nuovo oggetto
     */
    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    /**
     * Restituisce il testo del messaggio di notifica.
     *
     * @return corpo del messaggio
     */
    public String getMessaggio() {
        return messaggio;
    }

    /**
     * Imposta il testo del messaggio.
     *
     * @param messaggio nuovo testo
     */
    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    /**
     * Restituisce la data e ora della notifica.
     *
     * @return data e ora
     */
    public LocalDateTime getDataOra() {
        return dataOra;
    }

    /**
     * Imposta la data e ora della notifica.
     *
     * @param dataOra nuova data e ora
     */
    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    @Override
    public String toString() {
        return "\nOggetto: " + oggetto +
                "\n" + messaggio +  "[" + dataOra.format(formatter) + "]";
    }
}
