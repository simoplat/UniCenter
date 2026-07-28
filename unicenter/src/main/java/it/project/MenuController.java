package it.project;

import java.util.List;

public class MenuController {


    private final ConsoleUI console = ConsoleUI.getInstance();
    private final Unicenter unicenter;
    

    public MenuController() {
        this.unicenter = Unicenter.getInstance();

    }

    public void avvia() {
        boolean running = true;

        while (running) {
            console.mostraMessaggio("\n==========================================");
            console.mostraMessaggio("   UNICENTER - Gestione Universitaria   ");
            console.mostraMessaggio("==========================================");
            console.mostraMessaggio("1. Login");
            console.mostraMessaggio("2. Immatricolazione Nuovo Studente");
            console.mostraMessaggio("0. Esci dal sistema");

            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                case 1 -> loginUtente();
                case 2 -> gestisciImmatricolazione();
                case 0 -> {
                    console.mostraMessaggio("\nUscita dal sistema UniCenter. Arrivederci!");
                    running = false;
                    System.exit(0);
                }
                default -> console.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    // ==========================================
    // MENU AREA STUDENTE
    // ==========================================
    private void menuStudente() {
        boolean back = false;

        while (!back) {
            console.mostraMessaggio("\n------------------------------------------");
            console.mostraMessaggio("            AREA STUDENTE                 ");
            console.mostraMessaggio("------------------------------------------");
            console.mostraMessaggio("1. Iscriviti ad un appello d'esame");
            console.mostraMessaggio("0. Torna al menu principale");

            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                
                case 1 -> {
                    console.mostraMessaggio("\n--- Iscrizione Appello ---");
                    // Invocazione della catena di validazione e iscrizione
                    StampaAppelli(unicenter.visualizzaAppelliDisponibili());
                    String codiceAppello = console.leggiStringa("Inserisci il codice dell'appello al quale vuoi prenotarti");
                    if (!unicenter.iscriviStudenteAdAppello(codiceAppello)) {
                        console.mostraMessaggio("Codice appello non valido. Riprova.");
                        break;
                    } else {
                        console.mostraMessaggio("Iscrizione avvenuta con successo all'appello " + codiceAppello);
                    }

                }
                case 0 -> back = true;
                default -> console.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    // ==========================================
    // MENU AREA PROFESSORE / DOCENTE
    // ==========================================
    private void menuProfessore() {
        boolean back = false;

        while (!back) {
            console.mostraMessaggio("\n------------------------------------------");
            console.mostraMessaggio("         AREA PROFESSORE / DOCENTE        ");
            console.mostraMessaggio("------------------------------------------");
            console.mostraMessaggio("1. Crea nuovo appello d'esame");
            console.mostraMessaggio("2. Visualizza iscritti ad un appello");
            console.mostraMessaggio("0. Torna al menu principale");
            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                case 1 -> {
                    console.mostraMessaggio("\n--- Creazione Appello ---");
                    
                }
                case 2 -> {
                    console.mostraMessaggio("\n--- Lista Iscritti ---");
                    
                }
                case 0 -> back = true;
                default -> console.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    // ==========================================
    // SEGRETERIA / IMMATRICOLAZIONE
    // ==========================================
    private void gestisciImmatricolazione() {
        console.mostraMessaggio("\n------------------------------------------");
        console.mostraMessaggio("      IMMATRICOLAZIONE NUOVO STUDENTE     ");
        console.mostraMessaggio("------------------------------------------");
        //immatricolazioneController.immatricolaStudente();
    }

    



    public void loginUtente(){
        console.mostraMessaggio("\n------------------------------------------");
        console.mostraMessaggio("                 LOGIN                     ");
        console.mostraMessaggio("------------------------------------------");
        String email = console.leggiStringa("Inserisci email: ");
        if(!unicenter.esisteUtente(email)) {
            console.mostraMessaggio("Email non registrata. Riprova.");
            return;
        }
        String password = console.leggiStringa("Inserisci password: ");
        if(!unicenter.passwordCorretta(email, password)) {
            console.mostraMessaggio("Password errata. Riprova.");
            return;
        }
        console.mostraMessaggio("Login effettuato con successo!");
        console.mostraMessaggio("Benvenuto, " + unicenter.getCurrentUser().getNome() + "!");
        if(unicenter.getCurrentUser() instanceof Studente){
            menuStudente();
        } else if(unicenter.getCurrentUser() instanceof Professore){
            menuProfessore();
        }
        return;
    }

    public void StampaAppelli(List<Appello> appelliDisponibili) {
       console.mostraMessaggio(appelliDisponibili.toString());;
    }

}