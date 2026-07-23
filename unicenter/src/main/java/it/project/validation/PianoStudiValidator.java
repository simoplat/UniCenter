package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public class PianoStudiValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        // Logica di verifica presenza materia nel piano di studi
        boolean materiaPresente = studente.getPianoStudi() != null && 
                studente.getPianoStudi().contieneMateria(appello.getCodiceMateria());

        if (!materiaPresente) {
            throw new IllegalStateException("Iscrizione rifiutata: materia non presente nel piano di studi approvato.");
        }
        return checkNext(studente, appello);
    }
}