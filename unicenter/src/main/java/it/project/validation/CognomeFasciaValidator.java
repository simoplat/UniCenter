package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public class CognomeFasciaValidator extends AbstractIscrizioneValidator {
    @Override
    public boolean validate(Studente studente, Appello appello) throws Exception {
        String vincolo = appello.getVincoloLetteraCognome();
        
        if (vincolo != null && !vincolo.trim().isEmpty()) {
            String inizialeCognome = studente.getCognome().substring(0, 1).toUpperCase();
            if (!vincolo.toUpperCase().contains(inizialeCognome)) {
                throw new IllegalStateException("Iscrizione rifiutata: l'iniziale del cognome ('" 
                        + inizialeCognome + "') non rientra nella fascia stabilita ('" + vincolo + "').");
            }
        }
        return checkNext(studente, appello);
    }
}