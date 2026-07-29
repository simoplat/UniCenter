package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public class CognomeFasciaValidator extends AbstractIscrizioneValidator {

    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        // Se non vi sono vincoli sulla fascia o la fascia è null/vuota, la validazione passa
        String fascia = appello.getVincoloLetteraCognome();
        
        if (fascia != null && !fascia.trim().isEmpty()) {
            String cognome = studente.getCognome();
            
            if (cognome == null || cognome.trim().isEmpty()) {
                return false;
            }

            // 1. Estrai la prima lettera e convertila sempre in MAIUSCOLO
            char iniziale = Character.toUpperCase(cognome.trim().charAt(0));

            // 2. Separa il range (es. "A-Z", "A-L", "M-Z")
            String[] parti = fascia.split("-");
            
            if (parti.length == 2) {
                char da = Character.toUpperCase(parti[0].trim().charAt(0));
                char a = Character.toUpperCase(parti[1].trim().charAt(0));

                // 3. Verifica se l'iniziale si trova fuori dal range consentito [da, a]
                if (iniziale < da || iniziale > a) {
                    
                    return false;
                }
            }
        }

        // Se la validazione di questo anello ha successo, passa al prossimo validatore della catena
        return checkNext(studente, appello);
    }
}