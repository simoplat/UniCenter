package it.project.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import it.project.Amministratore;
import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Libretto;
import it.project.Materia;
import it.project.Notifica;
import it.project.PianoDiStudi;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.PostiNonValidi;
import it.project.view.ConsoleView;
import it.project.view.UniCenterView;
import it.project.view.server.UniCenterWebServer;

/**
 * Controller per la gestione dei flussi e dell'avvio dell'interfaccia utente
 * (Web Browser e Console).
 * Riprogettato con separazione delle responsabilità verso le componenti View e
 * Server,
 * mantenendo inalterata la logica di business e tutti i controlli del dominio.
 */
public class MenuController {

    private final UniCenterView view;
    private final Unicenter unicenter;
    private final UniCenterWebServer webServer;
    private final Scanner scanner = new Scanner(System.in);

    private final DateTimeFormatter formatterStampa = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
    private final DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter formatterInputData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Costruttore con console view di default.
     *
     * @param unicenter riferimento al sistema centrale UniCenter
     */
    public MenuController(Unicenter unicenter) {
        this(unicenter, new ConsoleView());
    }

    /**
     * Costruttore completo con vista personalizzata.
     *
     * @param unicenter riferimento al sistema centrale UniCenter
     * @param view      interfaccia di visualizzazione
     */
    public MenuController(Unicenter unicenter, UniCenterView view) {
        this.unicenter = unicenter;
        this.view = view != null ? view : new ConsoleView();
        this.webServer = new UniCenterWebServer(unicenter);
    }

    /**
     * Legge una riga di testo da console dopo aver mostrato un prompt.
     *
     * @param prompt messaggio di richiesta input
     * @return stringa inserita
     */
    public String leggiStringa(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    /**
     * Legge un numero intero da console gestendo errori di parsing.
     *
     * @param prompt messaggio di richiesta input
     * @return intero inserito
     */
    public int leggiIntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine())
                return 0;
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero intero valido.");
            }
        }
    }

    /**
     * Restituisce l'istanza del web server integrato.
     *
     * @return UniCenterWebServer
     */
    public UniCenterWebServer getWebServer() {
        return webServer;
    }

    /**
     * Restituisce la vista associata al controller.
     *
     * @return UniCenterView
     */
    public UniCenterView getView() {
        return view;
    }

    /**
     * Avvia il server Web e mantiene il processo attivo.
     */
    public void avvia() {
        // Avvia il server Web e apre direttamente il browser
        boolean serverStarted = webServer.start(8080);
        if (serverStarted) {
            view.mostraMessaggio("\n==========================================================");
            view.mostraMessaggio("   UNICENTER - Portale Web Avviato con Successo!          ");
            view.mostraMessaggio("   Interfaccia grafica aperta su: " + webServer.getBaseUrl());
            view.mostraMessaggio("==========================================================\n");
            webServer.openBrowser();
        } else {
            view.mostraErrore("Impossibile avviare il server Web.");
        }

        // Mantiene il processo attivo in ascolto delle richieste web
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            webServer.stop();
        }
    }

    // ==========================================
    // MENU AREA STUDENTE
    // ==========================================
    private void menuStudente() {
        boolean back = false;

        while (!back) {
            view.mostraMessaggio("\n------------------------------------------");
            view.mostraMessaggio("            AREA STUDENTE                 ");
            view.mostraMessaggio("------------------------------------------");
            view.mostraMessaggio("1. Iscriviti ad un appello d'esame");
            view.mostraMessaggio("2. Visualizza gli appelli a cui sei prenotato");
            view.mostraMessaggio("3. Visualizza notifiche");
            view.mostraMessaggio("4. Gestione esiti esami (Accetta/Rifiuta voto)");
            view.mostraMessaggio("5. Visualizza libretto");
            view.mostraMessaggio("6. Gestione tasse universitarie (Visualizza / Paga)");
            view.mostraMessaggio("7. Compila Piano di Studi (UC9)");
            view.mostraMessaggio("8. Rinnova iscrizione ad anno successivo");
            view.mostraMessaggio("0. Torna al menu principale");

            int scelta = leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                case 1 -> {
                    view.mostraMessaggio("\n--- Iscrizione Appello ---");
                    List<Appello> appelliDisponibili;
                    try {
                        appelliDisponibili = unicenter.trovaAppelliStudentePrenotabili();
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                        break;
                    }

                    if (appelliDisponibili == null || appelliDisponibili.isEmpty()) {
                        view.mostraMessaggio("Nessun appello disponibile al momento.");
                        break;
                    }
                    StampaAppelli(appelliDisponibili);
                    String codiceAppello = leggiStringa("Inserisci il codice dell'appello al quale vuoi prenotarti: ");

                    try {
                        if (unicenter.iscriviStudenteAdAppello(codiceAppello)) {
                            view.mostraMessaggio("Iscrizione avvenuta con successo all'appello " + codiceAppello);
                        } else {
                            view.mostraErrore("Iscrizione non riuscita.");
                        }
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }

                case 2 -> {
                    view.mostraMessaggio("\n--- I tuoi Appelli Prenotati ---");
                    List<Appello> appelliPrenotati = unicenter.trovaAppelliPrenotatiDalloStudente();
                    if (appelliPrenotati == null || appelliPrenotati.isEmpty()) {
                        view.mostraMessaggio("Non sei iscritto a nessun appello.");
                        break;
                    }
                    StampaAppelli(appelliPrenotati);

                    view.mostraMessaggio("1. Eliminare una prenotazione.");
                    view.mostraMessaggio("Inserisci altro valore intero per uscire.");

                    if (leggiIntero("Seleziona un'opzione: ") == 1) {
                        String codiceAppello = leggiStringa(
                                "Inserisci il codice dell'appello da cui vuoi eliminare la prenotazione: ");
                        try {
                            if (unicenter.disiscriviStudenteDaAppello(codiceAppello)) {
                                view.mostraMessaggio("Prenotazione eliminata con successo.");
                            } else {
                                view.mostraErrore("Impossibile eliminare la prenotazione.");
                            }
                        } catch (Exception e) {
                            view.mostraErrore(e.getMessage());
                        }
                    }
                }

                case 3 -> {
                    view.mostraMessaggio("\n--- Notifiche ---");
                    List<Notifica> notifiche = unicenter.getNotifichePerStudente();
                    if (notifiche == null || notifiche.isEmpty()) {
                        view.mostraMessaggio("Nessuna notifica disponibile.");
                    } else {
                        for (Notifica notifica : notifiche) {
                            view.mostraMessaggio(notifica.toString());
                        }
                    }
                }

                case 4 -> {
                    view.mostraMessaggio("\n--- Gestione Esiti Esami ---");
                    int rifiutatiAuto = unicenter.verificaScadenzeVoti();
                    if (rifiutatiAuto > 0) {
                        view.mostraMessaggio(
                                "[SISTEMA] " + rifiutatiAuto + " esito/i rifiutato/i automaticamente per scadenza.");
                    }

                    List<EsameSostenuto> esitiPendenti = unicenter.getEsitiPendentiStudente();
                    if (esitiPendenti == null || esitiPendenti.isEmpty()) {
                        view.mostraMessaggio("Non hai esiti in attesa di conferma.");
                        List<EsameSostenuto> tuttiEsiti = unicenter.getTuttiEsitiStudente();
                        if (tuttiEsiti != null && !tuttiEsiti.isEmpty()) {
                            view.mostraMessaggio("\n--- Storico Esiti ---");
                            stampaEsiti(tuttiEsiti);
                        }
                        break;
                    }

                    view.mostraMessaggio("Esiti in attesa di conferma:");
                    stampaEsiti(esitiPendenti);

                    String idVerbale = leggiStringa(
                            "Inserisci l'ID del verbale per cui vuoi esprimere la scelta (0 per uscire): ");
                    if ("0".equals(idVerbale))
                        break;

                    boolean esameValido = false;
                    for (EsameSostenuto e : esitiPendenti) {
                        if (e.getIdVerbale().equals(idVerbale)) {
                            esameValido = true;
                            break;
                        }
                    }
                    if (!esameValido) {
                        view.mostraErrore("ID verbale non valido o non in attesa di conferma.");
                        break;
                    }

                    view.mostraMessaggio("1. Accetta il voto");
                    view.mostraMessaggio("2. Rifiuta il voto");
                    int sceltaVoto = leggiIntero("Seleziona un'opzione: ");

                    switch (sceltaVoto) {
                        case 1 -> {
                            try {
                                if (unicenter.accettaVoto(idVerbale)) {
                                    view.mostraMessaggio("Voto ACCETTATO con successo! Registrato nel libretto.");
                                } else {
                                    view.mostraErrore("Impossibile accettare il voto.");
                                }
                            } catch (Exception e) {
                                view.mostraErrore(e.getMessage());
                            }
                        }
                        case 2 -> {
                            try {
                                if (unicenter.rifiutaVoto(idVerbale)) {
                                    view.mostraMessaggio("Voto RIFIUTATO. Potrai iscriverti a un appello futuro.");
                                } else {
                                    view.mostraErrore("Impossibile rifiutare il voto.");
                                }
                            } catch (Exception e) {
                                view.mostraErrore(e.getMessage());
                            }
                        }
                        default -> view.mostraMessaggio("Opzione non valida.");
                    }
                }

                case 5 -> visualizzaLibrettoStudente();

                case 6 -> {
                    view.mostraMessaggio("\n--- Gestione Tasse Universitarie ---");
                    double importoTasse = unicenter.getTasseStudente();
                    boolean pagate = unicenter.isTassePagateStudente();

                    view.mostraMessaggio("Importo totale tasse: " + String.format("%.2f €", importoTasse));
                    view.mostraMessaggio(
                            "Stato pagamento: " + (pagate ? "REGOLARE (Saldate)" : "IN SOSPESO (Non saldate)"));

                    if (pagate) {
                        view.mostraMessaggio("Le tasse universitarie risultano regolarmente saldate.");
                    } else {
                        view.mostraMessaggio(
                                "\n1. Simula pagamento delle tasse (" + String.format("%.2f €", importoTasse) + ")");
                        view.mostraMessaggio("0. Torna indietro");
                        int sceltaPaga = leggiIntero("Seleziona un'opzione: ");
                        if (sceltaPaga == 1) {
                            if (unicenter.pagaTasseStudente()) {
                                view.mostraMessaggio("Pagamento di " + String.format("%.2f €", importoTasse)
                                        + " completato con successo!");
                                view.mostraMessaggio(
                                        "Le tasse risultano ora SALDATE. Puoi procedere con l'iscrizione agli appelli.");
                            } else {
                                view.mostraErrore("Errore durante il pagamento delle tasse.");
                            }
                        }
                    }
                }

                case 7 -> gestisciCompilazionePianoStudi();
                case 8 -> gestisciRinnovoIscrizione();
                case 0 -> back = true;
                default -> view.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    private void gestisciRinnovoIscrizione() {
        view.mostraMessaggio("\n--- Rinnovo Iscrizione ad Anno Successivo ---");
        try {
            Map<String, Object> stato = unicenter.getStatoRinnovoStudenteCorrente();
            if (stato.isEmpty()) {
                view.mostraErrore("Nessuno studente autenticato.");
                return;
            }
            int annoAttuale = (int) stato.get("annoAttuale");
            int prossimoAnno = (int) stato.get("prossimoAnno");
            boolean isFuoriCorsoAttuale = (boolean) stato.get("isFuoriCorsoAttuale");
            boolean saraFuoriCorso = (boolean) stato.get("saraFuoriCorso");
            boolean finestraAperta = (boolean) stato.get("finestraAperta");
            boolean tassePregressePagate = (boolean) stato.get("tassePregressePagate");
            boolean giaRinnovato = (boolean) stato.get("giaRinnovato");
            double importoStimato = (double) stato.get("importoStimato");

            view.mostraMessaggio("Corso di Laurea: " + stato.get("nomeCorso"));
            view.mostraMessaggio("Anno accademico attuale: " + annoAttuale + "° anno ("
                    + (isFuoriCorsoAttuale ? "Fuori Corso" : "In Corso") + ")");
            view.mostraMessaggio("Prossimo anno di iscrizione: " + prossimoAnno + "° anno ("
                    + (saraFuoriCorso ? "Fuori Corso" : "In Corso") + ")");
            view.mostraMessaggio(
                    "Finestra temporale: " + (finestraAperta ? "APERTA (1 Settembre - 31 Dicembre)" : "CHIUSA"));
            view.mostraMessaggio("Tasse anno precedente: "
                    + (tassePregressePagate ? "REGOLARI (Saldate)" : "NON SALDATE (Debito pendente)"));
            view.mostraMessaggio(
                    "Rinnovo per ciclo corrente: " + (giaRinnovato ? "GIA' EFFETTUATO" : "NON ANCORA EFFETTUATO"));
            view.mostraMessaggio("Importo tasse stimato: " + String.format("%.2f €", importoStimato));

            if (!finestraAperta) {
                String motivo = (String) stato.get("motivoBlocco");
                view.mostraErrore(motivo != null ? motivo : "La finestra di rinnovo iscrizioni è attualmente chiusa.");
                return;
            }
            if (!tassePregressePagate) {
                view.mostraErrore(
                        "Impossibile rinnovare l'iscrizione: è necessario prima saldare le tasse dell'anno precedente.");
                return;
            }
            if (giaRinnovato) {
                view.mostraMessaggio("\nHai già rinnovato l'iscrizione per questo anno accademico.");
                return;
            }

            view.mostraMessaggio("\n1. Conferma ed esegui rinnovo iscrizione");
            view.mostraMessaggio("0. Annulla e torna indietro");
            int scelta = leggiIntero("Seleziona un'opzione: ");
            if (scelta == 1) {
                unicenter.rinnovaIscrizioneStudenteCorrente();
                view.mostraMessaggio("\n[SUCCESSO] Rinnovo iscrizione completato con successo!");
                view.mostraMessaggio("Sei ora iscritto al " + prossimoAnno + "° anno ("
                        + (saraFuoriCorso ? "Fuori Corso" : "In Corso") + ").");
                view.mostraMessaggio("Ricordati di procedere al pagamento delle tasse per il nuovo anno accademico.");
            }
        } catch (Exception e) {
            view.mostraErrore("Errore durante il rinnovo dell'iscrizione: " + e.getMessage());
        }
    }

    // =====================
    // MENU AREA PROFESSORE
    // =====================
    private void menuProfessore() {
        boolean back = false;

        while (!back) {
            view.mostraMessaggio("\n------------------------------------------");
            view.mostraMessaggio("         AREA PROFESSORE / DOCENTE        ");
            view.mostraMessaggio("------------------------------------------");
            view.mostraMessaggio("1. Crea nuovo appello d'esame");
            view.mostraMessaggio("2. Visualizza iscritti ad un appello");
            view.mostraMessaggio("3. Modifica appello.");
            view.mostraMessaggio("4. Elimina appello d'esame");
            view.mostraMessaggio("5. Pubblica esito esame (UC3)");
            view.mostraMessaggio("6. Visualizza esiti pubblicati");
            view.mostraMessaggio("7. Invia comunicazione / avviso di corso (UC7)");
            view.mostraMessaggio("0. Torna al menu principale");
            int scelta = leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                case 1 -> {
                    view.mostraMessaggio("\n--- Creazione Appello ---");
                    view.mostraMessaggio("Materie di cui sei professore:");
                    view.mostraMessaggio("------------------------------------------");
                    List<Materia> materieDelProfessore = unicenter.getMaterieDelProfessore();
                    if (materieDelProfessore == null || materieDelProfessore.isEmpty()) {
                        view.mostraMessaggio("Non sei abilitato a nessuna materia. Contatta l'amministratore.");
                        break;
                    }
                    stampaMaterie(materieDelProfessore);
                    String codiceMateria = leggiStringa(
                            "Inserisci il codice della materia per la quale vuoi creare l'appello: ");
                    if (!unicenter.isProfessoreAbilitatoAMateria(codiceMateria)) {
                        view.mostraMessaggio("Il codice inserito non è valido. Riprova.");
                        break;
                    }

                    String dataOraStr = leggiStringa(
                            "Inserisci la data e ora dell'appello (formato: dd/MM/yyyy HH:mm): ");
                    LocalDateTime dataOra;
                    try {
                        dataOra = LocalDateTime.parse(dataOraStr, formatterInput);
                    } catch (DateTimeParseException e) {
                        view.mostraErrore(
                                "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy HH:mm (es. 10/06/2026 09:30).");
                        break;
                    }

                    String aula = leggiStringa("Inserisci l'aula dell'appello: ");
                    int posti = leggiIntero("Inserisci il numero di posti disponibili: ");
                    String vincoloCognome = leggiStringa(
                            "Inserisci eventuale vincolo sul cognome (lascia vuoto se non necessario): ");

                    String termineIscrizione = leggiStringa(
                            "Inserisci la data di termine iscrizione (formato: dd/MM/yyyy): ");
                    LocalDate dataTermineIscrizione;
                    try {
                        dataTermineIscrizione = LocalDate.parse(termineIscrizione, formatterInputData);
                    } catch (DateTimeParseException e) {
                        view.mostraErrore(
                                "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy (es. 10/06/2026).");
                        break;
                    }

                    try {
                        unicenter.creaNuovoAppello(codiceMateria, dataOra, aula, posti, vincoloCognome,
                                dataTermineIscrizione);
                        view.mostraMessaggio("Appello creato con successo!");
                    } catch (DataNonValidaException | PostiNonValidi e) {
                        view.mostraErrore(e.getMessage());
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }

                case 2 -> {
                    view.mostraMessaggio("\n--- Lista Iscritti ---");
                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();
                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        view.mostraMessaggio("Non hai appelli disponibili.");
                        break;
                    }
                    StampaAppelli(appelliProfessore);

                    String app = leggiStringa("Seleziona il codice dell'appello di cui vuoi gli iscritti: ");
                    boolean appelloValido = false;
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(app)) {
                            appelloValido = true;
                            break;
                        }
                    }

                    if (!appelloValido) {
                        view.mostraMessaggio("Codice appello non valido. Riprova.");
                        break;
                    }

                    List<Studente> iscritti = unicenter.trovaIscrittiByAppello(app);
                    if (iscritti == null || iscritti.isEmpty()) {
                        view.mostraMessaggio("Non ci sono iscritti a questo appello.");
                    } else {
                        stampaStudenti(iscritti);
                    }
                }

                case 3 -> {
                    view.mostraMessaggio("\n--- Sezione di modifica appelli ---");
                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();
                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        view.mostraMessaggio("Non hai appelli da modificare.");
                        break;
                    }
                    StampaAppelli(appelliProfessore);

                    String idApp = leggiStringa(
                            "Seleziona il codice dell'appello da modificare (inserisci 0 per annullare): ");
                    if ("0".equals(idApp))
                        break;

                    Appello appelloTrovato = null;
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(idApp)) {
                            appelloTrovato = a;
                            break;
                        }
                    }

                    if (appelloTrovato == null) {
                        view.mostraErrore("Codice appello non valido. Riprova.");
                        break;
                    }

                    String nuovaDataOraStr = leggiStringa(
                            "Inserisci la data e ora dell'appello (formato: dd/MM/yyyy HH:mm): ");
                    LocalDateTime nuovaDataOra;
                    try {
                        nuovaDataOra = LocalDateTime.parse(nuovaDataOraStr, formatterInput);
                    } catch (DateTimeParseException e) {
                        view.mostraErrore(
                                "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy HH:mm (es. 10/06/2026 09:30).");
                        break;
                    }

                    String nuovaAula = leggiStringa("Inserisci aula dell'appello: ");
                    int nuoviPosti = leggiIntero("Inserisci posti disponibili: ");
                    String nuovoVincolo = leggiStringa(
                            "Inserisci eventuale vincolo sul cognome (lascia vuoto se non necessario): ");

                    String nuovoTermineIscrizioneStr = leggiStringa(
                            "Inserisci la nuova data di termine iscrizione (formato: dd/MM/yyyy): ");
                    LocalDate nuovoTermineIscrizione;
                    try {
                        nuovoTermineIscrizione = LocalDate.parse(nuovoTermineIscrizioneStr, formatterInputData);
                    } catch (DateTimeParseException e) {
                        view.mostraErrore(
                                "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy (es. 10/06/2026).");
                        break;
                    }

                    try {
                        if (unicenter.modificaAppello(appelloTrovato.getCodiceAppello(), nuovaDataOra, nuovaAula,
                                nuoviPosti, nuovoVincolo, nuovoTermineIscrizione)) {
                            view.mostraMessaggio("Appello modificato con successo.");
                        }
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }

                case 4 -> {
                    view.mostraMessaggio("\n--- Sezione di eliminazione appelli ---");
                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();
                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        view.mostraMessaggio("Non hai appelli da eliminare.");
                        break;
                    }
                    StampaAppelli(appelliProfessore);

                    String idApp = leggiStringa(
                            "Seleziona il codice dell'appello da eliminare (inserisci 0 per annullare): ");
                    if ("0".equals(idApp))
                        break;

                    Appello appelloTrovato = null;
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(idApp)) {
                            appelloTrovato = a;
                            break;
                        }
                    }

                    if (appelloTrovato == null) {
                        view.mostraErrore("Codice appello non valido o non trovato. Riprova.");
                        break;
                    }

                    try {
                        if (unicenter.eliminaAppello(appelloTrovato.getCodiceAppello())) {
                            view.mostraMessaggio("Appello eliminato con successo.");
                        } else {
                            view.mostraErrore("Errore durante l'eliminazione, riprovare.");
                        }
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }

                case 5 -> {
                    view.mostraMessaggio("\n--- Pubblica Esito Esame ---");
                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();
                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        view.mostraMessaggio("Non hai appelli disponibili.");
                        break;
                    }
                    StampaAppelli(appelliProfessore);

                    String codAppello = leggiStringa("Inserisci il codice dell'appello: ");
                    boolean appelloValido = false;
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(codAppello)) {
                            appelloValido = true;
                            break;
                        }
                    }
                    if (!appelloValido) {
                        view.mostraErrore("Codice appello non valido.");
                        break;
                    }

                    List<Studente> tuttiIscritti = unicenter.trovaIscrittiByAppello(codAppello);
                    if (tuttiIscritti == null || tuttiIscritti.isEmpty()) {
                        view.mostraMessaggio("Non ci sono studenti iscritti a questo appello.");
                        break;
                    }

                    String codiceMateriaAppello = null;
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(codAppello)) {
                            codiceMateriaAppello = a.getCodiceMateria();
                            break;
                        }
                    }

                    List<Studente> iscritti = new java.util.ArrayList<>();
                    for (Studente s : tuttiIscritti) {
                        boolean haEsitoPendente = false;
                        List<EsameSostenuto> esitiPendenti = unicenter.getEsitiPendentiByMatricola(s.getMatricola());
                        for (EsameSostenuto e : esitiPendenti) {
                            if (e.getCodiceMateria().equals(codiceMateriaAppello)) {
                                haEsitoPendente = true;
                                break;
                            }
                        }
                        if (!haEsitoPendente) {
                            iscritti.add(s);
                        }
                    }

                    if (iscritti.isEmpty()) {
                        view.mostraMessaggio(
                                "Tutti gli studenti iscritti hanno già un esito pendente per questa materia.");
                        break;
                    }
                    view.mostraMessaggio("\nStudenti iscritti (senza esito pendente):");
                    stampaStudenti(iscritti);

                    String matricola = leggiStringa("Inserisci la matricola dello studente: ");
                    boolean studenteValido = false;
                    for (Studente s : iscritti) {
                        if (s.getMatricola().equals(matricola)) {
                            studenteValido = true;
                            break;
                        }
                    }
                    if (!studenteValido) {
                        view.mostraErrore("Matricola non trovata tra gli iscritti.");
                        break;
                    }

                    int voto = leggiIntero("Inserisci il voto (0-30): ");
                    boolean lode = false;
                    if (voto == 30) {
                        String lodeStr = leggiStringa("Lode? (s/n): ");
                        lode = lodeStr.equalsIgnoreCase("s");
                    }

                    try {
                        EsameSostenuto esito = unicenter.pubblicaEsitoEsame(codAppello, matricola, codiceMateriaAppello,
                                voto, lode, 7);
                        view.mostraMessaggio("Esito pubblicato con successo!");
                        view.mostraMessaggio("ID Verbale: " + esito.getIdVerbale());
                        view.mostraMessaggio("Stato: " + esito.getNomeStato());
                        if (esito.getNomeStato().equals("Bocciato")) {
                            view.mostraMessaggio("(Voto insufficiente - Regola di Dominio 4)");
                        } else {
                            view.mostraMessaggio(
                                    "Scadenza conferma: " + esito.getScadenzaConferma().format(formatterStampa));
                        }
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }

                case 6 -> {
                    view.mostraMessaggio("\n--- Esiti Pubblicati ---");
                    List<EsameSostenuto> esitiProf = unicenter.getEsitiProfessore();
                    if (esitiProf == null || esitiProf.isEmpty()) {
                        view.mostraMessaggio("Non hai pubblicato nessun esito.");
                    } else {
                        stampaEsiti(esitiProf);
                    }
                }

                case 7 -> {
                    view.mostraMessaggio("\n--- Invia Comunicazione di Corso (UC7) ---");
                    List<Materia> materieProf = unicenter.getMaterieDelProfessore();
                    if (materieProf == null || materieProf.isEmpty()) {
                        view.mostraMessaggio("Non sei abilitato a nessuna materia.");
                        break;
                    }
                    stampaMaterie(materieProf);

                    String codMateria = leggiStringa(
                            "Inserisci il codice della materia per l'avviso (0 per annullare): ");
                    if ("0".equals(codMateria))
                        break;

                    if (!unicenter.isProfessoreAbilitatoAMateria(codMateria)) {
                        view.mostraErrore("Non sei abilitato a gestire questa materia.");
                        break;
                    }

                    List<Studente> destinatari = unicenter.getStudentiDestinatariComunicazione(codMateria);
                    view.mostraMessaggio(
                            "Studenti attualmente iscritti alla materia (destinatari): " + destinatari.size());

                    String titolo = leggiStringa("Inserisci il titolo/oggetto dell'avviso: ");
                    String messaggio = leggiStringa("Inserisci il testo della comunicazione: ");

                    try {
                        int notificati = unicenter.inviaComunicazioneMateria(codMateria, titolo, messaggio);
                        view.mostraMessaggio(
                                "\n[SUCCESSO] Comunicazione pubblicata e inviata a " + notificati + " studente/i.");
                    } catch (Exception e) {
                        view.mostraErrore("Errore durante l'invio della comunicazione: " + e.getMessage());
                    }
                }

                case 0 -> back = true;
                default -> view.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    // ==========================================
    // MENU AREA AMMINISTRATORE
    // ==========================================
    private void menuAmministratore() {
        boolean back = false;

        while (!back) {
            view.mostraMessaggio("\n------------------------------------------");
            view.mostraMessaggio("          AREA AMMINISTRATORE             ");
            view.mostraMessaggio("------------------------------------------");
            view.mostraMessaggio("1. Gestione Corsi di Laurea (UC4)");
            view.mostraMessaggio("2. Gestione Materie (UC5)");
            view.mostraMessaggio("3. Gestione Materie Pre-Approvate Corsi di Laurea (UC9)");
            view.mostraMessaggio("4. Valutazione Piani di Studi in Attesa (UC9)");
            view.mostraMessaggio("0. Torna al menu principale");

            int scelta = leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                case 1 -> gestisciCorsiAdmin();
                case 2 -> gestisciMaterieAdmin();
                case 3 -> gestisciMateriePreApprovateAdmin();
                case 4 -> gestisciApprovazionePianiAdmin();
                case 0 -> back = true;
                default -> view.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    private void gestisciCorsiAdmin() {
        boolean back = false;
        while (!back) {
            view.mostraMessaggio("\n--- Gestione Corsi di Laurea (UC4) ---");
            view.mostraMessaggio("1. Visualizza tutti i corsi di laurea");
            view.mostraMessaggio("2. Crea nuovo corso di laurea");
            view.mostraMessaggio("3. Modifica corso di laurea");
            view.mostraMessaggio("4. Rendi obsoleto un corso di laurea");
            view.mostraMessaggio("5. Elimina corso di laurea");
            view.mostraMessaggio("6. Finalizza corso di laurea");
            view.mostraMessaggio("7. Associa materia a corso non finalizzato");
            view.mostraMessaggio("0. Torna indietro");
            int op = leggiIntero("Seleziona opzione: ");

            switch (op) {
                case 1 -> stampaCorsiDiLaurea(unicenter.getCorsiDiLaurea());
                case 2 -> {
                    String nome = leggiStringa("Nome del corso: ");
                    String tipo = leggiStringa("Tipologia (es. Laurea Triennale, Magistrale): ");
                    int anni = leggiIntero("Anni accademici (es. 3 o 2 o 5): ");
                    try {
                        CorsoDiLaurea c = unicenter.creaCorsoDiLaurea(nome, tipo, anni);
                        view.mostraMessaggio("Corso creato con successo! Codice: " + c.getId());
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 3 -> {
                    String cod = leggiStringa("Codice del corso da modificare: ");
                    String nuovoNome = leggiStringa("Nuovo nome: ");
                    String nuovaTipo = leggiStringa("Nuova tipologia: ");
                    try {
                        if (unicenter.aggiornaCorsoDiLaurea(cod, nuovoNome, nuovaTipo)) {
                            view.mostraMessaggio("Corso aggiornato!");
                        }
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 4 -> {
                    String cod = leggiStringa("Codice del corso da rendere obsoleto: ");
                    try {
                        if (unicenter.rendiObsoletoCorsoDiLaurea(cod)) {
                            view.mostraMessaggio("Corso reso obsoleto!");
                        }
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 5 -> {
                    String cod = leggiStringa("Codice del corso da eliminare: ");
                    try {
                        if (unicenter.eliminaCorsoDiLaurea(cod)) {
                            view.mostraMessaggio("Corso eliminato!");
                        }
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 6 -> {
                    String cod = leggiStringa("Codice del corso da finalizzare: ");
                    try {
                        unicenter.finalizzaCorso(cod);
                        view.mostraMessaggio("Corso finalizzato con successo!");
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 7 -> {
                    List<CorsoDiLaurea> nonFin = unicenter.getCorsiNonFinalizzati();
                    if (nonFin.isEmpty()) {
                        view.mostraMessaggio("Nessun corso non finalizzato disponibile.");
                        break;
                    }
                    stampaCorsiDiLaurea(nonFin);
                    String codCorso = leggiStringa("Codice corso: ");
                    int anno = leggiIntero("Anno di corso a cui associare la materia: ");
                    List<Materia> tutteMat = unicenter.getTutteLeMaterie();
                    stampaMaterie(tutteMat);
                    String codMateria = leggiStringa("Codice materia: ");
                    Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(codMateria);
                    if (m == null) {
                        view.mostraErrore("Materia non trovata.");
                        break;
                    }
                    try {
                        unicenter.associaMateriaACorso(codCorso, anno, m);
                        view.mostraMessaggio("Materia associata con successo!");
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 0 -> back = true;
                default -> view.mostraMessaggio("Opzione non valida.");
            }
        }
    }

    private void gestisciMaterieAdmin() {
        boolean back = false;
        while (!back) {
            view.mostraMessaggio("\n--- Gestione Materie (UC5) ---");
            view.mostraMessaggio("1. Visualizza tutte le materie");
            view.mostraMessaggio("2. Crea nuova materia");
            view.mostraMessaggio("3. Associa professore a materia");
            view.mostraMessaggio("0. Torna indietro");
            int op = leggiIntero("Seleziona opzione: ");

            switch (op) {
                case 1 -> stampaMaterie(unicenter.getTutteLeMaterie());
                case 2 -> {
                    String nome = leggiStringa("Nome materia: ");
                    int cfu = leggiIntero("CFU (es. 6 o 9): ");
                    try {
                        Materia m = unicenter.creaMateria(nome, cfu);
                        view.mostraMessaggio("Materia creata! Codice: " + m.getCodiceMateria());
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 3 -> {
                    String codMateria = leggiStringa("Codice materia: ");
                    List<Professore> disponibili = unicenter.getProfessoriNonAssociatiAMateria(codMateria);
                    if (disponibili.isEmpty()) {
                        view.mostraMessaggio("Nessun professore disponibile per questa materia.");
                        break;
                    }
                    for (Professore p : disponibili) {
                        view.mostraMessaggio("ID: " + p.getIdProfessore() + " - " + p.getNome() + " " + p.getCognome());
                    }
                    String idProf = leggiStringa("ID professore da associare: ");
                    try {
                        unicenter.associaProfessoreAMateriaAdmin(idProf, codMateria);
                        view.mostraMessaggio("Professore associato con successo!");
                    } catch (Exception e) {
                        view.mostraErrore(e.getMessage());
                    }
                }
                case 0 -> back = true;
                default -> view.mostraMessaggio("Opzione non valida.");
            }
        }
    }

    // ==========================================
    // IMMATRICOLAZIONE
    // ==========================================
    void gestisciImmatricolazione() {
        view.mostraMessaggio("\n------------------------------------------");
        view.mostraMessaggio("      IMMATRICOLAZIONE NUOVO STUDENTE     ");
        view.mostraMessaggio("------------------------------------------");

        try {
            if (unicenter.validaDataImmatricolazione()) {
                view.mostraMessaggio(
                        "Finestra temporale per l'immatricolazione aperta. Procedi con l'immatricolazione.");
                List<CorsoDiLaurea> corsi = unicenter.getCorsiDiLaureaAttivi();
                if (corsi == null || corsi.isEmpty()) {
                    view.mostraMessaggio("Nessun corso di laurea attivo disponibile al momento.");
                    return;
                }
                view.mostraMessaggio("Corsi di laurea disponibili (solo attivi):");
                stampaCorsiDiLaurea(corsi);
            }
        } catch (DataNonValidaException e) {
            view.mostraErrore(e.getMessage());
            return;
        }

        String nome = leggiStringa("Inserisci il nome dello studente: ");
        String cognome = leggiStringa("Inserisci il cognome dello studente: ");
        String email = leggiStringa("Inserisci l'email dello studente: ");
        String password = leggiStringa("Inserisci la password di almeno 4 caratteri: ");
        String corsoDiLaurea = leggiStringa("Inserisci il nome del corso di laurea: ");

        try {
            unicenter.trovaCorsoDiLaureaByNome(corsoDiLaurea);
            String codiceFiscale = leggiStringa("Inserisci il tuo codice fiscale : ");

            Studente nuovoStudente = unicenter.immatricolaStudente(nome, cognome, email, password, corsoDiLaurea,
                    codiceFiscale);

            view.mostraMessaggio("\nIMMATRICOLAZIONE AVVENUTA CON SUCCESSO!");
            view.mostraMessaggio("La tua matrricola è: " + nuovoStudente.getMatricola());
            view.mostraMessaggio("Tasse da pagare: " + nuovoStudente.getTasse());
            view.mostraMessaggio("Il tuo codice fiscale è: " + codiceFiscale);
        } catch (CorsoDiLaureaNonTrovatoException e) {
            view.mostraErrore("Immatricolazione fallita: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            view.mostraErrore("immatricolazione fallita. " + e.getMessage());
        } catch (Exception e) {
            view.mostraErrore("immatricolazione fallita. " + e.getMessage());
        }
    }

    /**
     * Esegue il login dell'utente da console.
     */
    public void loginUtente() {
        view.mostraMessaggio("\n------------------------------------------");
        view.mostraMessaggio("                 LOGIN                     ");
        view.mostraMessaggio("------------------------------------------");
        String email = leggiStringa("Inserisci email: ");
        if (!unicenter.esisteUtente(email)) {
            view.mostraMessaggio("Email non registrata. Riprova.");
            return;
        }
        String password = leggiStringa("Inserisci password: ");
        if (!unicenter.passwordCorretta(email, password)) {
            view.mostraMessaggio("Password errata. Riprova.");
            return;
        }
        view.mostraMessaggio("Login effettuato con successo!");
        view.mostraMessaggio("Benvenuto, " + unicenter.getCurrentUser().getNome() + "!");
        if (unicenter.getCurrentUser() instanceof Studente) {
            menuStudente();
        } else if (unicenter.getCurrentUser() instanceof Professore) {
            menuProfessore();
        } else if (unicenter.getCurrentUser() instanceof Amministratore) {
            menuAmministratore();
        }
    }

    /**
     * Stampa la lista degli appelli d'esame.
     *
     * @param appelliDisponibili lista appelli
     */
    public void StampaAppelli(List<Appello> appelliDisponibili) {
        view.stampaAppelli(appelliDisponibili);
    }

    /**
     * Stampa la lista delle materie.
     *
     * @param materie lista materie
     */
    public void stampaMaterie(List<Materia> materie) {
        view.stampaMaterie(materie);
    }

    /**
     * Stampa la lista degli studenti.
     *
     * @param studenti lista studenti
     */
    public void stampaStudenti(List<Studente> studenti) {
        view.stampaStudenti(studenti);
    }

    /**
     * Stampa la lista dei corsi di laurea.
     *
     * @param corsi lista corsi
     */
    public void stampaCorsiDiLaurea(List<CorsoDiLaurea> corsi) {
        view.stampaCorsiDiLaurea(corsi);
    }

    /**
     * Stampa la lista degli esiti d'esame.
     *
     * @param esiti lista esiti sostenuti
     */
    public void stampaEsiti(List<EsameSostenuto> esiti) {
        view.stampaEsiti(esiti);
    }

    private void gestisciCompilazionePianoStudi() {
        view.mostraMessaggio("\n--- Compilazione Piano di Studi (UC9) ---");
        Studente studente = (unicenter.getCurrentUser() instanceof Studente) ? (Studente) unicenter.getCurrentUser()
                : null;
        if (studente == null) {
            view.mostraErrore("Nessuno studente autenticato.");
            return;
        }

        try {
            unicenter.getPianoStudiController().verificaVincoloCompilazioneMaterie(studente);
        } catch (IllegalStateException e) {
            view.mostraErrore(e.getMessage());
            return;
        }

        List<Materia> disponibili = unicenter.getMaterieASceltaDisponibili();
        if (disponibili.isEmpty()) {
            view.mostraMessaggio("Nessuna materia a scelta disponibile.");
            return;
        }

        stampaMaterie(disponibili);
        String sceltiStr = leggiStringa(
                "Inserisci i codici delle materie a scelta separati da virgola (minimo 12 CFU): ");
        if (sceltiStr.trim().isEmpty() || "0".equals(sceltiStr.trim()))
            return;

        List<String> codici = new java.util.ArrayList<>();
        for (String s : sceltiStr.split(",")) {
            if (!s.trim().isEmpty())
                codici.add(s.trim());
        }

        try {
            boolean ok = unicenter.compilaPianoDiStudi(codici);
            if (ok) {
                view.mostraMessaggio("Piano di studi inviato con successo! Stato attuale: "
                        + studente.getPianoDiStudi().getNomeStato());
            }
        } catch (Exception e) {
            view.mostraErrore(e.getMessage());
        }
    }

    private void gestisciMateriePreApprovateAdmin() {
        view.mostraMessaggio("\n--- Gestione Materie Pre-Approvate Corsi di Laurea (UC9) ---");
        List<CorsoDiLaurea> corsi = unicenter.getCorsiDiLaurea();
        stampaCorsiDiLaurea(corsi);
        String codCorso = leggiStringa("Inserisci il codice del corso (0 per annullare): ");
        if ("0".equals(codCorso))
            return;

        List<Materia> pre = unicenter.getMateriePreApprovateByCorso(codCorso);
        view.mostraMessaggio("Materie attualmente pre-approvate (" + pre.size() + "):");
        stampaMaterie(pre);

        view.mostraMessaggio("1. Aggiungi materia pre-approvata");
        view.mostraMessaggio("2. Rimuovi materia pre-approvata");
        int op = leggiIntero("Seleziona opzione: ");

        if (op == 1) {
            String codMat = leggiStringa("Codice materia da aggiungere: ");
            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(codMat);
            if (m != null) {
                try {
                    unicenter.aggiungiMateriaPreApprovata(codCorso, m);
                    view.mostraMessaggio("Materia aggiunta alle pre-approvate!");
                } catch (Exception e) {
                    view.mostraErrore(e.getMessage());
                }
            } else {
                view.mostraErrore("Materia non trovata.");
            }
        } else if (op == 2) {
            String codMat = leggiStringa("Codice materia da rimuovere: ");
            Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(codMat);
            if (m != null) {
                try {
                    unicenter.rimuoviMateriaPreApprovata(codCorso, m);
                    view.mostraMessaggio("Materia rimossa dalle pre-approvate!");
                } catch (Exception e) {
                    view.mostraErrore(e.getMessage());
                }
            } else {
                view.mostraErrore("Materia non trovata.");
            }
        }
    }

    private void gestisciApprovazionePianiAdmin() {
        view.mostraMessaggio("\n--- Valutazione Piani di Studi in Attesa (UC9) ---");
        java.util.Map<String, it.project.PianoDiStudi> pianiInAttesa = unicenter.getPianiInAttesaApprovazione();
        if (pianiInAttesa == null || pianiInAttesa.isEmpty()) {
            view.mostraMessaggio("Nessun piano di studi in attesa di approvazione.");
            return;
        }

        view.mostraMessaggio("Piani in attesa di approvazione (" + pianiInAttesa.size() + "):");
        for (java.util.Map.Entry<String, it.project.PianoDiStudi> entry : pianiInAttesa.entrySet()) {
            view.mostraMessaggio(
                    "Matricola: " + entry.getKey() + " - Materie a scelta: " + entry.getValue().getIdMaterieAScelta());
        }

        String matricola = leggiStringa("Inserisci matricola da valutare (0 per annullare): ");
        if ("0".equals(matricola))
            return;

        view.mostraMessaggio("1. APPROVA piano");
        view.mostraMessaggio("2. RIFIUTA piano");
        int scelta = leggiIntero("Seleziona: ");
        try {
            if (scelta == 1) {
                unicenter.approvaPianoDiStudi(matricola);
                view.mostraMessaggio("Piano approvato con successo!");
            } else if (scelta == 2) {
                unicenter.rifiutaPianoDiStudi(matricola);
                view.mostraMessaggio("Piano rifiutato!");
            }
        } catch (Exception e) {
            view.mostraErrore(e.getMessage());
        }
    }

    private void visualizzaLibrettoStudente() {
        Studente studente = (unicenter.getCurrentUser() instanceof Studente) ? (Studente) unicenter.getCurrentUser()
                : null;
        if (studente == null) {
            view.mostraErrore("Nessuno studente autenticato.");
            return;
        }
        Libretto libretto = studente.getLibretto();
        PianoDiStudi piano = studente.getPianoDiStudi();
        view.mostraMessaggio("\n--- Libretto Universitario ---");
        view.mostraMessaggio("Totale CFU: " + (libretto != null ? libretto.getTotaleCfu() : 0));
        view.mostraMessaggio("Esami superati: " + (libretto != null ? libretto.getNumeroEsamiSuperati() : 0));
        if (libretto != null && libretto.getNumeroEsamiSuperati() > 0) {
            view.mostraMessaggio(String.format("Media ponderata: %.2f/30", libretto.getMediaPonderata()));
        }

        if (piano != null) {
            String stato = piano.getNomeStato();
            String statoDisplay = ("Registrato".equalsIgnoreCase(stato) || "Approvato".equalsIgnoreCase(stato))
                    ? "Approvato"
                    : stato;
            view.mostraMessaggio("\n--- Materie a Scelta (Stato Piano: " + statoDisplay + ") ---");
            if ("Rifiutato".equalsIgnoreCase(stato) || piano.getIdMaterieAScelta().isEmpty()) {
                view.mostraMessaggio(
                        "Nessuna materia a scelta selezionata, compilare il piano di studi per inserirle.");
            } else {
                for (String cod : piano.getIdMaterieAScelta()) {
                    Materia m = unicenter.getGestoreMaterie().trovaMaterieByCodice(cod);
                    String nome = m != null ? m.getNome() : cod;
                    int cfu = m != null ? m.getCfu() : 0;
                    boolean superato = libretto != null && libretto.isEsameSuperato(cod);
                    view.mostraMessaggio("- [" + cod + "] " + nome + " (" + cfu + " CFU) - "
                            + (superato ? "SUPERATO" : "DA SOSTENERE"));
                }
            }
        }
    }
}
