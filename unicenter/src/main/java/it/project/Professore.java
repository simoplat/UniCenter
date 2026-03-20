package it.project;

import it.project.exceptions.UtenteNonTrovatoException;

public class Professore extends Utente {

    public Professore(int id, String nome, String cognome, String email, String password, String codiceFiscale) {
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
        consoleUi.mostraMessaggio("Procedura di aggiunta appello avviata.");
        try {
            Unicenter.getInstance().creaAppello(getId());
            
            
        } catch (UtenteNonTrovatoException e) {
            consoleUi.mostraErrore("Non è stato possibile aggiungere un Appello");
        }
        
    }

    private void modificaAppello() {
        consoleUi.mostraMessaggio("Modifica dell'appello selezionato.");
    }

    private void rimuoviAppello() {
        consoleUi.mostraMessaggio("Appello rimosso con successo.");
    }
}