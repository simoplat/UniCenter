package it.project.validation;

import it.project.Appello;
import it.project.Studente;

/**
 * Validatore nella Chain of Responsibility: verifica che lo studente non abbia
 * già superato e verbalizzato nel libretto l'esame della materia.
 */
public class EsameSuperatoValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        if (studente.getLibretto() != null && studente.getLibretto().isEsameSuperato(appello.getCodiceMateria())) {
            throw new IllegalStateException("Iscrizione rifiutata: esame già superato e registrato nel libretto.");
        }
        return checkNext(studente, appello);
    }
}
