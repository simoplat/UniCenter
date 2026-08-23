package it.project.exceptions;

/**
 * Eccezione unchecked lanciata quando un appello d'esame richiesto non viene trovato nel sistema.
 */
public class AppelloNonTrovatoException extends RuntimeException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo dell'errore
     */
    public AppelloNonTrovatoException(String message) {
        super(message);
    }
}
