package it.project.validation;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.validator.IscrizioneNonValidaException;
import it.project.exceptions.validator.TasseNonPagateException;

/**
 * Validatore Chain of Responsibility: verifica che lo studente sia in regola con il pagamento delle tasse universitarie.
 */
public class TassaPaidValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException {
        if (!studente.isTassePagate()) {
            throw new TasseNonPagateException("Iscrizione rifiutata: tasse universitarie non saldate.");
        }
        return checkNext(studente, appello);
    }
}