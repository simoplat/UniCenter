package it.project.exceptions.validator;

/**
 * Eccezione controllata (checked) lanciata quando una materia obbligatoria è prevista per un anno di corso futuro
 * rispetto all'anno corrente di iscrizione dello studente.
 */
public class AnnoCorsoNonValidoException extends IscrizioneNonValidaException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public AnnoCorsoNonValidoException(String message) {
        super(message);
    }
}
