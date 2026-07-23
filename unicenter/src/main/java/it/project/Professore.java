package it.project;

import it.project.exceptions.UtenteNonTrovatoException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Professore extends Utente {

    public Professore(String id, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(id, nome, cognome, email, password, codiceFiscale);
    }

    // Metodo principale per il professore
    @Override
    public void menuPersonale() {
        consoleUi.mostraMessaggio("Benvenuto Professore " + toString());
        int run = 1;
        while (run == 1) {
            consoleUi.mostraMessaggio("1) Visualizza Appelli");
            consoleUi.mostraMessaggio("2) Aggiungi un Appello");
            consoleUi.mostraMessaggio("3) Modifica un Appello");
            consoleUi.mostraMessaggio("4) Rimuovi un Appello");
            consoleUi.mostraMessaggio("5) Esci");

            Integer scelta = consoleUi.leggiIntero("Seleziona un'opzione: ");
            try {
                switch (scelta) {
                    case 1:
                        visualizzaAppelli();
                        break;
                    case 2:
                        aggiungiAppello();
                        break;
                    case 3:
                        modificaAppello();
                        break;
                    case 4:
                        rimuoviAppello();
                        break;
                    case 5:
                        consoleUi.mostraMessaggio("Uscita dal programma...");
                        run = 0;
                        break;
                    default:
                        consoleUi.mostraMessaggio("Opzione non valida, riprova.");
                        break;
                }
            } catch (NumberFormatException e) {
                consoleUi.mostraErrore("Inserisci un valore intero, riprova.");

            } catch (Exception e) {
                consoleUi.mostraErrore("Si è verificato un errore, riprova.");
            }
        }

    }

    // Sottomenu specifico per la bacheca
    public void menuAppelli() {

    }

    // Metodi di supporto
    private void visualizzaAppelli() {
        consoleUi.mostraMessaggio("Visualizzazione degli appelli in corso...");
    }

    private void aggiungiAppello() {
    consoleUi.mostraMessaggio("=== Procedura di aggiunta appello ===");
    
    try {
        // 1. Lettura dei dati da console
        String codiceMateria = consoleUi.leggiStringa("Inserisci il codice della materia (es. IS01): ");
        String strDataOra = consoleUi.leggiStringa("Inserisci la data e ora dell'appello (formato: yyyy-MM-dd HH:mm): ");
        
        // 2. Parsing della Stringa in LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dataOra = LocalDateTime.parse(strDataOra, formatter);

        String aula = consoleUi.leggiStringa("Inserisci l'aula dell'appello: ");
        int posti = consoleUi.leggiIntero("Inserisci il numero di posti disponibili: ");
        String vincoloCognome = consoleUi.leggiStringa("Inserisci il vincolo di cognome (es. A-M, N-Z o premi Invio per nessuno): ");

        // 3. Chiamata al metodo di dominio
        Appello appelloCreato = Unicenter.getInstance().creaNuovoAppello(
                codiceMateria, 
                dataOra, 
                aula, 
                posti, 
                vincoloCognome
        );

        consoleUi.mostraMessaggio("Appello aggiunto con successo! Codice identificativo: " + appelloCreato.getCodiceAppello());

    } catch (DateTimeParseException e) {
        consoleUi.mostraErrore("Formato data/ora non valido! Assicurati di usare il formato yyyy-MM-dd HH:mm (es. 2026-09-15 09:30).");
    } catch (IllegalArgumentException e) {
        consoleUi.mostraErrore("Errore nei dati inseriti: " + e.getMessage());
    } catch (Exception e) {
        consoleUi.mostraErrore("Non è stato possibile aggiungere l'appello: " + e.getMessage());
    }
}

    private void modificaAppello() {
        consoleUi.mostraMessaggio("Modifica dell'appello selezionato.");
    }

    private void rimuoviAppello() {
        consoleUi.mostraMessaggio("Appello rimosso con successo.");
    }
}