package it.project.validation;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.DataNonValidaException;
import java.time.LocalDate;

import it.project.database.ClockProvider;

/**
 * Validatore Chain of Responsibility: verifica che la richiesta di iscrizione avvenga
 * entro i termini massimi stabiliti per l'appello.
 */
public class DataTermineIscrizioneValidator extends AbstractIscrizioneValidator {

    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        LocalDate oggi = ClockProvider.nowLocalDate();
        LocalDate termineIscrizione = appello.getTermineIscrizione(); // o appello.getRegistrationDeadline()

        // 1. Controllo presenza del termine di iscrizione
        if (termineIscrizione == null) {
            throw new DataNonValidaException("Impossibile procedere: data di scadenza iscrizioni non definita per l'appello.");
        }

        // 2. Controllo finestra temporale (se oggi è dopo la data limite)
        if (oggi.isAfter(termineIscrizione)) {
            throw new DataNonValidaException(
                "Iscrizione respinta: le iscrizioni per questo appello si sono chiuse il " + termineIscrizione + "."
            );
        }
        return checkNext(studente, appello);
    }

  
}