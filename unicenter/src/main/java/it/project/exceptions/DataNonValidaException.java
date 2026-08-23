package it.project.exceptions;

/**
 * Eccezione controllata (checked) lanciata quando una data non rispetta i vincoli temporali o di calendario accademico.
 */
public class DataNonValidaException extends Exception {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public DataNonValidaException(String message) {
        super(message);
    }

}
