package it.project.exceptions;

/**
 * Eccezione unchecked lanciata quando un corso di laurea non viene trovato nel sistema.
 */
public class CorsoDiLaureaNonTrovatoException extends RuntimeException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public CorsoDiLaureaNonTrovatoException(String message) {
        super(message);
    }
}
