package it.project.validation;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.validator.FasciaCognomeNonValidaException;
import it.project.exceptions.validator.IscrizioneNonValidaException;

/**
 * Validatore Chain of Responsibility: verifica che l'iniziale del cognome dello studente
 * ricada all'interno della fascia alfabetica (es. A-L o M-Z) configurata per l'appello.
 */
public class CognomeFasciaValidator extends AbstractIscrizioneValidator {

    @Override
    public boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException {
        String fascia = appello.getVincoloLetteraCognome();
        
        if (fascia != null && !fascia.trim().isEmpty()) {
            String cognome = studente.getCognome();
            
            if (cognome == null || cognome.trim().isEmpty()) {
                throw new FasciaCognomeNonValidaException("Iscrizione rifiutata: cognome dello studente non disponibile.");
            }

            char iniziale = Character.toUpperCase(cognome.trim().charAt(0));

            String[] parti = fascia.split("-");
            
            if (parti.length == 2) {
                char da = Character.toUpperCase(parti[0].trim().charAt(0));
                char a = Character.toUpperCase(parti[1].trim().charAt(0));

                if (iniziale < da || iniziale > a) {
                    throw new FasciaCognomeNonValidaException("Iscrizione rifiutata: il cognome non rientra nella fascia " + fascia + " dell'appello.");
                }
            }
        }
        return checkNext(studente, appello);
    }
}