package it.project.exceptions.validator;

/**
 * Superclasse controllata (checked) per tutte le eccezioni specifiche lanciate
 * dai validatori della Chain of Responsibility durante l'iscrizione a un appello.
 */
public class IscrizioneNonValidaException extends Exception {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public IscrizioneNonValidaException(String message) {
        super(message);
    }
}
