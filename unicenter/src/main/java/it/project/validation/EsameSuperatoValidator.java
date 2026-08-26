package it.project.validation;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.validator.EsameGiaSuperatoException;
import it.project.exceptions.validator.IscrizioneNonValidaException;

/**
 * Validatore nella Chain of Responsibility: verifica che lo studente non abbia
 * già superato e verbalizzato nel libretto l'esame della materia.
 */
public class EsameSuperatoValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException {
        if (studente.getLibretto() != null && studente.getLibretto().isEsameSuperato(appello.getCodiceMateria())) {
            throw new EsameGiaSuperatoException("Iscrizione rifiutata: esame già superato e registrato nel libretto.");
        }
        return checkNext(studente, appello);
    }
}
