package it.project.exceptions.validator;

/**
 * Eccezione controllata (checked) lanciata quando la materia richiesta per l'appello non è inclusa
 * nel piano di studi dello studente oppure quando una materia a scelta richiede un piano approvato.
 */
public class PianoStudiNonValidoException extends IscrizioneNonValidaException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public PianoStudiNonValidoException(String message) {
        super(message);
    }
}
