package it.project.exceptions.validator;

/**
 * Eccezione controllata (checked) lanciata quando lo studente tenta di iscriversi a un appello
 * senza essere in regola con il pagamento delle tasse universitarie.
 */
public class TasseNonPagateException extends IscrizioneNonValidaException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public TasseNonPagateException(String message) {
        super(message);
    }
}
