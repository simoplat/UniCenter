package it.project.exceptions;

import it.project.exceptions.validator.IscrizioneNonValidaException;

/**
 * Eccezione controllata (checked) lanciata quando il numero di posti specificato per un appello non è valido o i posti sono esauriti.
 */
public class PostiNonValidi extends IscrizioneNonValidaException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public PostiNonValidi(String message) {
        super(message);
    }

}
