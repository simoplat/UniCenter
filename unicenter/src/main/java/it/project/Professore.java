package it.project;
import java.util.Scanner;


public class Professore extends Utente {

    // Metodo principale per il professore
    public void menuPersonale(Scanner sc) {
        int scelta = -1;

        while (scelta != 0) {
            try {
                System.out.println("\n--- MENU PROFESSORE ---");
                System.out.println("1 - Gestione Bacheca Appelli");
                System.out.println("0 - Logout");
                System.out.print("> ");

                scelta = Integer.parseInt(sc.nextLine());

                switch (scelta) {
                    case 1:
                        menuAppelli(sc);
                        break;
                    case 0:
                        System.out.println("Ritorno al menu principale...");
                        break;
                    default:
                        System.out.println("Opzione non valida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Errore: Inserisci un numero valido.");
            } catch (Exception e) {
                System.out.println("Si è verificato un errore imprevisto: " + e.getMessage());
            }
        }
    }

    // Sottomenu specifico per la bacheca
    public void menuAppelli(Scanner sc) {
        int scelta = -1;

        while (scelta != 0) {
            try {
                System.out.println("\n--- BACHECA APPELLI ---");
                System.out.println("1 - Visualizza appelli");
                System.out.println("2 - Aggiungi appello");
                System.out.println("3 - Modifica appello");
                System.out.println("4 - Rimuovi appello");
                System.out.println("0 - Torna indietro");
                System.out.print("> ");

                scelta = Integer.parseInt(sc.nextLine());

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
                    case 0:
                        System.out.println("Uscita dalla bacheca.");
                        break;
                    default:
                        System.out.println("Opzione non valida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Errore: Inserisci un valore valido.");
            } catch (Exception e) {
                System.out.println("Si è verificato un errore imprevisto: " + e.getMessage());
            }
        }
    }

    // Metodi di supporto
    private void visualizzaAppelli() {
        System.out.println("Visualizzazione degli appelli in corso...");
    }

    private void aggiungiAppello() {
        System.out.println("Procedura di aggiunta appello avviata.");
    }

    private void modificaAppello() {
        System.out.println("Modifica dell'appello selezionato.");
    }

    private void rimuoviAppello() {
        System.out.println("Appello rimosso con successo.");
    }
}