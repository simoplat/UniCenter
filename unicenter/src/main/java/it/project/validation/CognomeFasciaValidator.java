package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public class CognomeFasciaValidator extends AbstractIscrizioneValidator {

    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        String fascia = appello.getVincoloLetteraCognome();
        
        if (fascia != null && !fascia.trim().isEmpty()) {
            String cognome = studente.getCognome();
            
            if (cognome == null || cognome.trim().isEmpty()) {
                return false;
            }

            char iniziale = Character.toUpperCase(cognome.trim().charAt(0));

            String[] parti = fascia.split("-");
            
            if (parti.length == 2) {
                char da = Character.toUpperCase(parti[0].trim().charAt(0));
                char a = Character.toUpperCase(parti[1].trim().charAt(0));

                if (iniziale < da || iniziale > a) {
                    throw new IllegalStateException("Iscrizione rifiutata: il cognome non rientra nella fascia " + fascia + " dell'appello.");
                }
            }
        }
        return checkNext(studente, appello);
    }
}