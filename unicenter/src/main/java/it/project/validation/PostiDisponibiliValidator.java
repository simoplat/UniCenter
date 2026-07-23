package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public class PostiDisponibiliValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        if (appello.getPostiDisponibili() <= 0) {
            throw new IllegalStateException("Iscrizione rifiutata: posti esauriti per l'appello.");
        }
        return checkNext(studente, appello);
    }
}