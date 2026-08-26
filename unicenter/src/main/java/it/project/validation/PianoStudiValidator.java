package it.project.validation;

import it.project.Appello;
import it.project.PianoDiStudi;
import it.project.Studente;
import it.project.exceptions.validator.IscrizioneNonValidaException;
import it.project.exceptions.validator.PianoStudiNonValidoException;

/**
 * Validatore Chain of Responsibility: verifica la presenza della materia nel piano di studi
 * e l'approvazione del piano per le materie a scelta.
 */
public class PianoStudiValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException {
        PianoDiStudi piano = studente.getPianoDiStudi();
        String codiceMateria = appello.getCodiceMateria();
        if (piano == null || !piano.contieneMateria(codiceMateria)) {
            throw new PianoStudiNonValidoException("Iscrizione rifiutata: materia non presente nel piano di studi.");
        }
        // UC9: materie a scelta richiedono piano approvato
        if (piano.isMateriaAScelta(codiceMateria) && !piano.isApprovato()) {
            throw new PianoStudiNonValidoException(
                "Iscrizione rifiutata: il piano di studi non è ancora approvato. "
                + "Le materie a scelta richiedono l'approvazione del piano.");
        }
        return checkNext(studente, appello);
    }
}