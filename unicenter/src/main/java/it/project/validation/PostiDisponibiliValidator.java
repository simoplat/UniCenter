package it.project.validation;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.PostiNonValidi;
import it.project.exceptions.validator.IscrizioneNonValidaException;

/**
 * Validatore Chain of Responsibility: verifica che vi siano posti disponibili rimanenti per l'appello.
 */
public class PostiDisponibiliValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException {
        if (appello.getPostiDisponibili() <= 0) {
            throw new PostiNonValidi("Iscrizione rifiutata: posti esauriti per l'appello.");
        }
        return checkNext(studente, appello);
    }
}