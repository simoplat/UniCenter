package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public class TassaPaidValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        if (!studente.isTassePagate()) {
            throw new IllegalStateException("Iscrizione rifiutata: tasse universitarie non saldate.");
        }
        return checkNext(studente, appello);
    }
}