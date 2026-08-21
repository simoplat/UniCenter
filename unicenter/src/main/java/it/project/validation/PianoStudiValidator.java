package it.project.validation;

import it.project.Appello;
import it.project.PianoDiStudi;
import it.project.Studente;

public class PianoStudiValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        PianoDiStudi piano = studente.getPianoDiStudi();
        String codiceMateria = appello.getCodiceMateria();
        if (piano == null || !piano.contieneMateria(codiceMateria)) {
            throw new IllegalStateException("Iscrizione rifiutata: materia non presente nel piano di studi.");
        }
        // UC9: materie a scelta richiedono piano approvato
        if (piano.isMateriaAScelta(codiceMateria) && !piano.isApprovato()) {
            throw new IllegalStateException(
                "Iscrizione rifiutata: il piano di studi non è ancora approvato. "
                + "Le materie a scelta richiedono l'approvazione del piano.");
        }
        return checkNext(studente, appello);
    }
}