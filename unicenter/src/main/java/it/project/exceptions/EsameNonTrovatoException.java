package it.project.exceptions;

/**
 * Eccezione unchecked lanciata quando un verbale o un esame sostenuto non viene trovato nel sistema.
 */
public class EsameNonTrovatoException extends RuntimeException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public EsameNonTrovatoException(String message) {
        super(message);
    }
}
