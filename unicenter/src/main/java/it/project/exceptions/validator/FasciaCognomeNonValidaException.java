package it.project.exceptions.validator;

/**
 * Eccezione controllata (checked) lanciata quando l'iniziale del cognome dello studente non rientra
 * nella fascia alfabetica configurata per l'appello d'esame.
 */
public class FasciaCognomeNonValidaException extends IscrizioneNonValidaException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public FasciaCognomeNonValidaException(String message) {
        super(message);
    }
}
