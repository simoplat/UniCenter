package it.project.validation;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.validator.IscrizioneNonValidaException;

/**
 * Base Handler astratto per il Pattern Chain of Responsibility (GoF Comportamentale).
 * Gestisce l'inoltro della richiesta al validatore successivo nella catena.
 */
public abstract class AbstractIscrizioneValidator implements IscrizioneValidator {
    protected IscrizioneValidator next;

    @Override
    public void setNext(IscrizioneValidator nextValidator) {
        this.next = nextValidator;
    }

    /**
     * Inoltra la validazione all'anello successivo se presente.
     *
     * @param studente studente da validare
     * @param appello  appello richiesto
     * @return true se tutti i validatori successivi hanno esito positivo
     * @throws IscrizioneNonValidaException in caso di violazione nei validatori successivi
     */
    protected boolean checkNext(Studente studente, Appello appello) throws IscrizioneNonValidaException {
        if (next == null) {
            return true;
        }
        return next.validate(studente, appello);
    }
}