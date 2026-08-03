package it.project.controller;

import java.util.List;

import it.project.Appello;
import it.project.ConsoleUI;
import it.project.CorsoDiLaurea;
import it.project.Materia;
import it.project.Notifica;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.PostiNonValidi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MenuController {

    private final ConsoleUI console = ConsoleUI.getInstance();
    private Unicenter unicenter;
    DateTimeFormatter formatterStampa = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
    DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MenuController(Unicenter unicenter) {
        this.unicenter = unicenter;
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
            console.mostraMessaggio("2. Visualizza gli appelli a cui sei prenotato");
            console.mostraMessaggio("3. Visualizza notifiche");
            console.mostraMessaggio("0. Torna al menu principale");

            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {

                case 1 -> {
                    console.mostraMessaggio("\n--- Iscrizione Appello ---");
                    // Invocazione della catena di validazione e iscrizione
                    List<Appello> appelliDisponibili = unicenter.trovaAppelliStudentePrenotabili();
                    if (appelliDisponibili == null || appelliDisponibili.isEmpty()) {
                        console.mostraMessaggio("Nessun appello disponibile al momento.");
                        break;
                    }
                    StampaAppelli(appelliDisponibili);
                    String codiceAppello = console
                            .leggiStringa("Inserisci il codice dell'appello al quale vuoi prenotarti: ");
                    if (!unicenter.iscriviStudenteAdAppello(codiceAppello)) {
                        console.mostraMessaggio("Codice appello non valido. Riprova.");
                        break;
                    } else {
                        console.mostraMessaggio("Iscrizione avvenuta con successo all'appello " + codiceAppello);
                    }

                }

                case 2 -> {
                    console.mostraMessaggio("\n--- I tuoi Appelli Prenotati ---");
                    List<Appello> appelliPrenotati = unicenter.trovaAppelliPrenotatiDalloStudente();
                    if (appelliPrenotati == null || appelliPrenotati.isEmpty()) {
                        console.mostraMessaggio("Non sei iscritto a nessun appello.");
                        break;
                    }
                    StampaAppelli(appelliPrenotati);

                    console.mostraMessaggio("1. Eliminare una prenotazione.");
                    console.mostraMessaggio("Inserisci altro valore intero per uscire.");

                    switch (console.leggiIntero("Seleziona un'opzione: ")) {
                        case 1 -> {
                            String codiceAppello = console.leggiStringa(
                                    "Inserisci il codice dell'appello da cui vuoi eliminare la prenotazione: ");
                            if (!unicenter.disiscriviStudenteDaAppello(codiceAppello)) {
                                console.mostraMessaggio("Codice appello non valido. Riprova.");
                            } else {
                                console.mostraMessaggio("Prenotazione eliminata con successo.");
                            }
                            break;
                        }
                        case 0 -> {
                            break;
                        }
                        default -> console.mostraMessaggio("Opzione non valida. Riprova.");
                    }

                }

                case 3 -> {
                    console.mostraMessaggio("\n--- Notifiche ---");
                    List<Notifica> notifiche = unicenter.getNotifichePerStudente();
                    if (notifiche == null || notifiche.isEmpty()) {
                        console.mostraMessaggio("Nessuna notifica disponibile.");
                    } else {
                        for (Notifica notifica : notifiche) {
                            console.mostraMessaggio(notifica.toString());
                        }
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
            console.mostraMessaggio("3. Modifica appello.");
            console.mostraMessaggio("4. Elimina appello d'esame");
            console.mostraMessaggio("0. Torna al menu principale");
            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                case 1 -> {
                    console.mostraMessaggio("\n--- Creazione Appello ---");
                    console.mostraMessaggio("Materie di cui sei professore:");
                    console.mostraMessaggio("------------------------------------------");
                    List<Materia> materieDelProfessore = unicenter.getMaterieDelProfessore();
                    StampaMaterie(materieDelProfessore);
                    String codiceMateria = console
                            .leggiStringa("Inserisci il codice della materia per la quale vuoi creare l'appello: ");
                    if (!unicenter.isProfessoreAbilitatoAMateria(codiceMateria)) {
                        console.mostraMessaggio("Il codice inserito non è valido. Riprova.");
                        break;
                    }

                    String dataOraStr = console
                            .leggiStringa("Inserisci la data e ora dell'appello (formato: yyyy-MM-dd HH:mm): ");
                    LocalDateTime dataOra = null;
                    // 2. Esegui il parsing (gestendo eventuali errori di input dell'utente)
                    try {
                        dataOra = LocalDateTime.parse(dataOraStr, formatterInput);
                        console.mostraMessaggio("Data e ora convertite con successo: " + dataOra);
                    } catch (DateTimeParseException e) {
                        console.mostraErrore(
                                "Errore: Formato data non valido! Assicurati di usare il formato yyyy-MM-dd HH:mm (es. 2026-06-15 09:30).");
                        break;
                    }

                    String aula = console.leggiStringa("Inserisci l'aula dell'appello: ");
                    int posti = console.leggiIntero("Inserisci il numero di posti disponibili: ");
                    String vincoloCognome = console
                            .leggiStringa("Inserisci eventuale vincolo sul cognome (lascia vuoto se non necessario): ");
                    String codiceAppello = unicenter.generaCodiceAppello();
                    Appello nuovoAppello = new Appello(codiceAppello, codiceMateria, dataOra, aula, posti,
                            vincoloCognome);

                    try {
                        unicenter.creaNuovoAppello(nuovoAppello);
                        console.mostraMessaggio("Appello creato con successo! Codice Appello: " + codiceAppello);

                    } catch (DataNonValidaException e) {
                        console.mostraErrore("[ERRORE CREAZIONE APPELLO] " + e.getMessage());
                        break;
                    } catch (PostiNonValidi e) {
                        console.mostraErrore("[ERRORE CREAZIONE APPELLO] " + e.getMessage());
                        break;
                    } catch (Exception e) {
                        console.mostraErrore("[ERRORE CREAZIONE APPELLO] Errore imprevisto: " + e.getMessage());
                        break;
                    }

                    break;
                }
                case 2 -> {
                    console.mostraMessaggio("\n--- Lista Iscritti ---");
                    console.mostraMessaggio("I tuoi appelli:");
                    StampaAppelli(unicenter.trovaAppelliProfessore());
                    String app = console.leggiStringa("Seleziona il codice dell'appello di cui vuoi gli iscritti: ");
                    List<Studente> iscritti = unicenter.trovaIscrittiByAppello(app);

                    if (iscritti == null || iscritti.size() == 0) {
                        console.mostraMessaggio("Non ci sono iscritti a questo appello.");
                    } else {
                        stampaStudenti(iscritti);
                    }
                    break;

                }
                case 3 -> {
                    console.mostraMessaggio("\n--- Sezione di modifica appelli ---");
                    console.mostraMessaggio("------------------------------------------");

                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();

                    console.mostraMessaggio("I tuoi appelli:");

                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        console.mostraMessaggio("Non hai appelli da modificare.");
                        break;
                    } else {
                        StampaAppelli(appelliProfessore);
                    }

                    Appello appelloTrovato = null;

                    while (appelloTrovato == null) {
                        String idApp = console.leggiStringa(
                                "Seleziona il codice dell'appello da modificare (inserisci 0 per annullare): ");

                        if ("0".equals(idApp)) {
                            console.mostraMessaggio("Operazione annullata.");
                            break;
                        }

                        for (Appello a : appelliProfessore) {
                            if (a.getCodiceAppello().equals(idApp)) {
                                appelloTrovato = a;
                                break;
                            }
                        }
                        if (appelloTrovato == null) {
                            console.mostraErrore(" Codice appello non valido o non trovato. Riprova.");
                        }

                    }

                    if (appelloTrovato != null) {
                        String nuovaDataOraStr = console
                                .leggiStringa("Inserisci la data e ora dell'appello (formato: yyyy-MM-dd HH:mm): ");
                        LocalDateTime nuovaDataOra = null;
                        try {
                            nuovaDataOra = LocalDateTime.parse(nuovaDataOraStr, formatterInput);
                            console.mostraMessaggio("Data e ora convertite con successo: " + nuovaDataOra);
                        } catch (DateTimeParseException e) {
                            console.mostraErrore(
                                    "Errore: Formato data non valido! Assicurati di usare il formato yyyy-MM-dd HH:mm (es. 2026-06-15 09:30).");
                            break;
                        }

                        String nuovaAula = console.leggiStringa("Inserisci aula dell'appello: ");
                        int nuoviPosti = console.leggiIntero("Inserisci posti disponibili: ");
                        String nuovoVincolo = console.leggiStringa(
                                "Inserisci eventuale vincolo sul cognome (lascia vuoto se non necessario): ");

                        if (unicenter.modificaAppello(appelloTrovato.getCodiceAppello(), nuovaDataOra, nuovaAula,
                                nuoviPosti, nuovoVincolo)) {
                            console.mostraMessaggio("Appello modificato con successo.");
                            break;
                        } else {
                            console.mostraMessaggio("Qualcosa è andato storto. Riprova.");
                            break;
                        }
                    }
                }
                case 4 -> {
                    console.mostraMessaggio("\n--- Sezione di modifica appelli ---");
                    console.mostraMessaggio("------------------------------------------");

                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();

                    console.mostraMessaggio("I tuoi appelli:");

                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        console.mostraMessaggio("Non hai appelli da modificare.");
                        break;
                    } else {
                        StampaAppelli(appelliProfessore);
                    }

                    Appello appelloTrovato = null;

                    while (appelloTrovato == null) {

                        String idApp = console.leggiStringa(
                                "Seleziona il codice dell'appello da eliminare (inserisci 0 per annullare): ");

                        if ("0".equals(idApp)) {
                            console.mostraMessaggio("Operazione annullata.");
                            break;
                        }

                        for (Appello a : appelliProfessore) {
                            if (a.getCodiceAppello().equals(idApp)) {
                                appelloTrovato = a;
                                break;
                            }
                        }
                        if (appelloTrovato == null) {
                            console.mostraErrore(" Codice appello non valido o non trovato. Riprova.");
                        }
                    }

                    if (appelloTrovato != null) {
                        if (unicenter.eliminaAppello(appelloTrovato.getCodiceAppello())) {
                            console.mostraMessaggio("Appello eliminato con successo.");
                            break;
                        } else {
                            console.mostraMessaggio("Errore durante l'eliminazione, riprovare.");
                            break;
                        }
                    }
                }
                case 0 -> back = true;
                default -> console.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }

    }

    // ==========================================
    //  IMMATRICOLAZIONE
    // ==========================================
    private void gestisciImmatricolazione() {
        console.mostraMessaggio("\n------------------------------------------");
        console.mostraMessaggio("      IMMATRICOLAZIONE NUOVO STUDENTE     ");
        console.mostraMessaggio("------------------------------------------");
        String nome = console.leggiStringa("Inserisci il nome dello studente: ");
        String cognome = console.leggiStringa("Inserisci il cognome dello studente: ");
        String email = console.leggiStringa("Inserisci l'email dello studente: ");
        String password = console.leggiStringa("Inserisci la password di almeno 4 caratteri: ");
        String corsoDiLaurea = console.leggiStringa("Inserisci il corso di laurea : ");
        
        CorsoDiLaurea corso = unicenter.trovaCorsoDiLaureaByNome(corsoDiLaurea);
       
        if (corso == null) {
            console.mostraMessaggio("Corso di laurea non trovato.");
            return;
        }

        String codiceFiscale = console.leggiStringa("Inserisci il tuo codice fiscale : ");
        double tassaBaseCorso = 500.0; 

        Studente nuovoStudente = unicenter.immatricolaStudente(nome, cognome, email, password, corsoDiLaurea, tassaBaseCorso, codiceFiscale);

        console.mostraMessaggio("\nIMMATRICOLAZIONE AVVENUTA CON SUCCESSO!");
        console.mostraMessaggio("La tua matrricola è: " + nuovoStudente.getMatricola());
        console.mostraMessaggio("Tasse da pagare: " + nuovoStudente.getTotaleTasse());
    }

    public void loginUtente() {
        console.mostraMessaggio("\n------------------------------------------");
        console.mostraMessaggio("                 LOGIN                     ");
        console.mostraMessaggio("------------------------------------------");
        String email = console.leggiStringa("Inserisci email: ");
        if (!unicenter.esisteUtente(email)) {
            console.mostraMessaggio("Email non registrata. Riprova.");
            return;
        }
        String password = console.leggiStringa("Inserisci password: ");
        if (!unicenter.passwordCorretta(email, password)) {
            console.mostraMessaggio("Password errata. Riprova.");
            return;
        }
        console.mostraMessaggio("Login effettuato con successo!");
        console.mostraMessaggio("Benvenuto, " + unicenter.getCurrentUser().getNome() + "!");
        if (unicenter.getCurrentUser() instanceof Studente) {
            menuStudente();
        } else if (unicenter.getCurrentUser() instanceof Professore) {
            menuProfessore();
        }
        return;
    }

    public void StampaAppelli(List<Appello> appelliDisponibili) {
        for (Appello appello : appelliDisponibili) {
            // Formatta la data
            String dataOraFormattata = appello.getDataOra().format(formatterStampa);

            console.mostraMessaggio(
                    "Codice Appello: " + appello.getCodiceAppello() + "\n" +
                            "Materia: " + appello.getCodiceMateria() + "\n" +
                            "Data e Ora: " + dataOraFormattata + "\n" +
                            "Aula: " + appello.getAula() + "\n" +
                            "Posti Disponibili: " + appello.getPostiDisponibili() + "\n" +
                            "Vincolo Cognome: "
                            + (appello.getVincoloLetteraCognome() != null ? appello.getVincoloLetteraCognome()
                                    : "Nessuno")
                            + "\n" +
                            "----------------------------------------");
        }
    }

    public void StampaMaterie(List<Materia> materie) {
        for (Materia materia : materie) {
            console.mostraMessaggio(
                    "Codice Materia: " + materia.getCodiceMateria() + "\n" +
                            "Nome Materia: " + materia.getNome() + "\n" +
                            "----------------------------------------");
        }
    }

    public void stampaStudenti(List<Studente> studenti) {
        for (Studente studente : studenti) {
            console.mostraMessaggio(
                    studente.getNome() + " - " + studente.getCognome() + " - " + studente.getCodiceFiscale() + "\n" +
                            "----------------------------------------");
        }
    }

}