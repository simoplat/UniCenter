package it.project.exceptions;

/**
 * Eccezione controllata (checked) lanciata quando il numero di posti specificato per un appello non è valido.
 */
public class PostiNonValidi extends Exception {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public PostiNonValidi(String message) {
        super(message);
    }

}
