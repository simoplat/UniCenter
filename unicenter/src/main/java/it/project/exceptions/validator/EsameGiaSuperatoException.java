package it.project.exceptions.validator;

/**
 * Eccezione controllata (checked) lanciata quando lo studente tenta di iscriversi a un appello
 * per una materia il cui esame risulta già superato e verbalizzato nel libretto.
 */
public class EsameGiaSuperatoException extends IscrizioneNonValidaException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public EsameGiaSuperatoException(String message) {
        super(message);
    }
}
