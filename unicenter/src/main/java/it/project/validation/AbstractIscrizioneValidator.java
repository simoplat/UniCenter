package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public abstract class AbstractIscrizioneValidator implements IscrizioneValidator {
    protected IscrizioneValidator next;

    @Override
    public void setNext(IscrizioneValidator nextValidator) {
        this.next = nextValidator;
    }

    protected boolean checkNext(Studente studente, Appello appello) throws Exception {
        if (next == null) {
            return true;
        }
        return next.validate(studente, appello);
    }
}