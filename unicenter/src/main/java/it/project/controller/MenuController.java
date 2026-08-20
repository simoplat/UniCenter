package it.project.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import it.project.Amministratore;
import it.project.Appello;
import it.project.ConsoleUI;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Libretto;
import it.project.Materia;
import it.project.Notifica;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.PostiNonValidi;
import it.project.factory.CorsoDiLaureaFactory;

public class MenuController {

    private final ConsoleUI console = ConsoleUI.getInstance();
    private Unicenter unicenter;
    DateTimeFormatter formatterStampa = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
    DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    DateTimeFormatter formatterInputData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
            console.mostraMessaggio("4. Gestione esiti esami (Accetta/Rifiuta voto)");
            console.mostraMessaggio("5. Visualizza libretto");
            console.mostraMessaggio("6. Gestione tasse universitarie (Visualizza / Paga)");
            console.mostraMessaggio("0. Torna al menu principale");

            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {

                case 1 -> {
                    console.mostraMessaggio("\n--- Iscrizione Appello ---");
                    List<Appello> appelliDisponibili;
                    try {
                        appelliDisponibili = unicenter.trovaAppelliStudentePrenotabili();
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                        break;
                    }

                    if (appelliDisponibili == null || appelliDisponibili.isEmpty()) {
                        console.mostraMessaggio("Nessun appello disponibile al momento.");
                        break;
                    }
                    StampaAppelli(appelliDisponibili);
                    String codiceAppello = console
                            .leggiStringa("Inserisci il codice dell'appello al quale vuoi prenotarti: ");

                    try {
                        if (unicenter.iscriviStudenteAdAppello(codiceAppello)) {
                            console.mostraMessaggio("Iscrizione avvenuta con successo all'appello " + codiceAppello);
                        } else {
                            console.mostraErrore("Iscrizione non riuscita.");
                        }
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
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
                            try {
                                if (unicenter.disiscriviStudenteDaAppello(codiceAppello)) {
                                    console.mostraMessaggio("Prenotazione eliminata con successo.");
                                } else {
                                    console.mostraErrore("Impossibile eliminare la prenotazione.");
                                }
                            } catch (Exception e) {
                                console.mostraErrore(e.getMessage());
                            }
                            break;
                        }
                        case 0 -> {
                            break;
                        }
                        default -> {
                            break;
                        }
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

                // ============================================================
                // UC3 - GESTIONE ESITI ESAMI (Accetta / Rifiuta voto)
                // ============================================================
                case 4 -> {
                    console.mostraMessaggio("\n--- Gestione Esiti Esami ---");

                    // Verifica scadenze (Estensione A: Silenzio Rifiuto)
                    int rifiutatiAuto = unicenter.verificaScadenzeVoti();
                    if (rifiutatiAuto > 0) {
                        console.mostraMessaggio(
                                "[SISTEMA] " + rifiutatiAuto + " esito/i rifiutato/i automaticamente per scadenza.");
                    }

                    List<EsameSostenuto> esitiPendenti = unicenter.getEsitiPendentiStudente();
                    if (esitiPendenti == null || esitiPendenti.isEmpty()) {
                        console.mostraMessaggio("Non hai esiti in attesa di conferma.");

                        // Mostra anche lo storico completo
                        List<EsameSostenuto> tuttiEsiti = unicenter.getTuttiEsitiStudente();
                        if (tuttiEsiti != null && !tuttiEsiti.isEmpty()) {
                            console.mostraMessaggio("\n--- Storico Esiti ---");
                            stampaEsiti(tuttiEsiti);
                        }
                        break;
                    }

                    console.mostraMessaggio("Esiti in attesa di conferma:");
                    stampaEsiti(esitiPendenti);

                    String idEsame = console.leggiStringa(
                            "Inserisci l'ID dell'esame per cui vuoi esprimere la scelta (0 per uscire): ");
                    if ("0".equals(idEsame)) {
                        break;
                    }

                    // Verifica che l'ID appartenga agli esiti pendenti
                    boolean esameValido = false;
                    for (EsameSostenuto e : esitiPendenti) {
                        if (e.getIdEsame().equals(idEsame)) {
                            esameValido = true;
                            break;
                        }
                    }
                    if (!esameValido) {
                        console.mostraErrore("ID esame non valido o non in attesa di conferma.");
                        break;
                    }

                    console.mostraMessaggio("1. Accetta il voto");
                    console.mostraMessaggio("2. Rifiuta il voto");
                    int sceltaVoto = console.leggiIntero("Seleziona un'opzione: ");

                    switch (sceltaVoto) {
                        case 1 -> {
                            try {
                                if (unicenter.accettaVoto(idEsame)) {
                                    console.mostraMessaggio("Voto ACCETTATO con successo! Registrato nel libretto.");
                                } else {
                                    console.mostraErrore("Impossibile accettare il voto.");
                                }
                            } catch (Exception e) {
                                console.mostraErrore(e.getMessage());
                            }
                        }
                        case 2 -> {
                            try {
                                if (unicenter.rifiutaVoto(idEsame)) {
                                    console.mostraMessaggio("Voto RIFIUTATO. Potrai iscriverti a un appello futuro.");
                                } else {
                                    console.mostraErrore("Impossibile rifiutare il voto.");
                                }
                            } catch (Exception e) {
                                console.mostraErrore(e.getMessage());
                            }
                        }
                        default -> console.mostraMessaggio("Opzione non valida.");
                    }
                }

                // ============================================================
                // UC3 - VISUALIZZA LIBRETTO (Information Expert)
                // ============================================================
                case 5 -> {
                    console.mostraMessaggio("\n--- Il tuo Libretto ---");
                    Libretto libretto = unicenter.getLibrettoStudente();
                    if (libretto == null || libretto.getNumeroEsamiSuperati() == 0) {
                        console.mostraMessaggio("Il libretto è vuoto. Nessun esame registrato.");
                    } else {
                        console.mostraMessaggio("Esami superati: " + libretto.getNumeroEsamiSuperati());
                        console.mostraMessaggio("CFU totali: " + libretto.getTotaleCfu());
                        console.mostraMessaggio(
                                String.format("Media ponderata: %.2f/30", libretto.getMediaPonderata()));
                        console.mostraMessaggio("------------------------------------------");
                        for (EsameSostenuto esame : libretto.getEsamiSuperati()) {
                            console.mostraMessaggio(
                                    "Materia: " + esame.getCodiceMateria()
                                            + " | Voto: " + esame.getVotoNumerico()
                                            + (esame.isLode() ? " e Lode" : "")
                                            + " | CFU: " + esame.getCfu()
                                            + " | Data: " + esame.getDataRegistrazione().format(formatterStampa));
                        }
                        console.mostraMessaggio("------------------------------------------");
                    }
                }

                // ============================================================
                // GESTIONE TASSE UNIVERSITARIE (Visualizza / Paga)
                // ============================================================
                case 6 -> {
                    console.mostraMessaggio("\n--- Gestione Tasse Universitarie ---");
                    double importoTasse = unicenter.getTasseStudente();
                    boolean pagate = unicenter.isTassePagateStudente();

                    console.mostraMessaggio("Importo totale tasse: " + String.format("%.2f €", importoTasse));
                    console.mostraMessaggio("Stato pagamento: " + (pagate ? "REGOLARE (Saldate)" : "IN SOSPESO (Non saldate)"));

                    if (pagate) {
                        console.mostraMessaggio("Le tasse universitarie risultano regolarmente saldate.");
                    } else {
                        console.mostraMessaggio("\n1. Simula pagamento delle tasse (" + String.format("%.2f €", importoTasse) + ")");
                        console.mostraMessaggio("0. Torna indietro");
                        int sceltaPaga = console.leggiIntero("Seleziona un'opzione: ");
                        if (sceltaPaga == 1) {
                            if (unicenter.pagaTasseStudente()) {
                                console.mostraMessaggio("Pagamento di " + String.format("%.2f €", importoTasse) + " completato con successo!");
                                console.mostraMessaggio("Le tasse risultano ora SALDATE. Puoi procedere con l'iscrizione agli appelli.");
                            } else {
                                console.mostraErrore("Errore durante il pagamento delle tasse.");
                            }
                        }
                    }
                }

                case 0 -> back = true;
                default -> console.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    // =====================
    // MENU AREA PROFESSORE
    // =====================
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
            console.mostraMessaggio("5. Pubblica esito esame (UC3)");
            console.mostraMessaggio("6. Visualizza esiti pubblicati");
            console.mostraMessaggio("7. Invia comunicazione / avviso di corso (UC7)");
            console.mostraMessaggio("0. Torna al menu principale");
            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                case 1 -> {
                    console.mostraMessaggio("\n--- Creazione Appello ---");
                    console.mostraMessaggio("Materie di cui sei professore:");
                    console.mostraMessaggio("------------------------------------------");
                    List<Materia> materieDelProfessore = unicenter.getMaterieDelProfessore();
                    if (materieDelProfessore == null || materieDelProfessore.isEmpty()) {
                        console.mostraMessaggio("Non sei abilitato a nessuna materia. Contatta l'amministratore.");
                        break;
                    }
                    stampaMaterie(materieDelProfessore);
                    String codiceMateria = console
                            .leggiStringa("Inserisci il codice della materia per la quale vuoi creare l'appello: ");
                    if (!unicenter.isProfessoreAbilitatoAMateria(codiceMateria)) {
                        console.mostraMessaggio("Il codice inserito non è valido. Riprova.");
                        break;
                    }

                    String dataOraStr = console
                            .leggiStringa("Inserisci la data e ora dell'appello (formato: dd/MM/yyyy HH:mm): ");
                    LocalDateTime dataOra = null;
                    try {
                        dataOra = LocalDateTime.parse(dataOraStr, formatterInput);
                    } catch (DateTimeParseException e) {
                        console.mostraErrore(
                                "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy HH:mm (es. 10/06/2026 09:30).");
                        break;
                    }

                    String aula = console.leggiStringa("Inserisci l'aula dell'appello: ");
                    int posti = console.leggiIntero("Inserisci il numero di posti disponibili: ");
                    String vincoloCognome = console
                            .leggiStringa("Inserisci eventuale vincolo sul cognome (lascia vuoto se non necessario): ");

                    String termineIscrizione = console
                            .leggiStringa("Inserisci la data di termine iscrizione (formato: dd/MM/yyyy): ");
                    LocalDate dataTermineIscrizione = null;
                    try {
                        dataTermineIscrizione = LocalDate.parse(termineIscrizione, formatterInputData);
                    } catch (DateTimeParseException e) {
                        console.mostraErrore(
                                "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy (es. 10/06/2026).");
                        break;
                    }

                    try {
                        unicenter.creaNuovoAppello(codiceMateria, dataOra, aula, posti, vincoloCognome,
                                dataTermineIscrizione);
                        console.mostraMessaggio("Appello creato con successo!");

                    } catch (DataNonValidaException e) {
                        console.mostraErrore(e.getMessage());
                        break;
                    } catch (PostiNonValidi e) {
                        console.mostraErrore(e.getMessage());
                        break;
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                        break;
                    }

                    break;
                }
                case 2 -> {
                    console.mostraMessaggio("\n--- Lista Iscritti ---");
                    console.mostraMessaggio("I tuoi appelli:");

                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();
                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        console.mostraMessaggio("Non hai appelli disponibili.");
                        break;
                    }
                    StampaAppelli(appelliProfessore);

                    String app = console.leggiStringa("Seleziona il codice dell'appello di cui vuoi gli iscritti: ");

                    boolean appelloValido = false;

                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(app)) {
                            appelloValido = true;
                            break;
                        }
                    }

                    if (!appelloValido) {
                        console.mostraMessaggio("Codice appello non valido. Riprova.");
                        break;
                    }

                    List<Studente> iscritti = unicenter.trovaIscrittiByAppello(app);

                    if (iscritti == null || iscritti.isEmpty()) {
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

                    String idApp = console.leggiStringa(
                            "Seleziona il codice dell'appello da modificare (inserisci 0 per annullare): ");

                    if ("0".equals(idApp)) {
                        console.mostraMessaggio("Operazione annullata.");
                        break;
                    }

                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(idApp)) {
                            appelloTrovato = a;
                        }
                    }

                    if (appelloTrovato == null) {
                        console.mostraErrore(" Codice appello non valido. Riprova.");
                        break;
                    }

                    if (appelloTrovato != null) {
                        String nuovaDataOraStr = console
                                .leggiStringa("Inserisci la data e ora dell'appello (formato: dd/MM/yyyy HH:mm): ");
                        LocalDateTime nuovaDataOra = null;
                        try {
                            nuovaDataOra = LocalDateTime.parse(nuovaDataOraStr, formatterInput);
                        } catch (DateTimeParseException e) {
                            console.mostraErrore(
                                    "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy HH:mm (es. 10/06/2026 09:30).");
                            break;
                        }

                        String nuovaAula = console.leggiStringa("Inserisci aula dell'appello: ");
                        int nuoviPosti = console.leggiIntero("Inserisci posti disponibili: ");
                        String nuovoVincolo = console.leggiStringa(
                                "Inserisci eventuale vincolo sul cognome (lascia vuoto se non necessario): ");

                        String nuovoTermineIscrizioneStr = console
                                .leggiStringa("Inserisci la nuova data di termine iscrizione (formato: dd/MM/yyyy): ");
                        LocalDate nuovoTermineIscrizione = null;
                        try {
                            nuovoTermineIscrizione = LocalDate.parse(nuovoTermineIscrizioneStr, formatterInputData);
                        } catch (DateTimeParseException e) {
                            console.mostraErrore(
                                    "Formato data non valido! Assicurati di usare il formato dd/MM/yyyy (es. 10/06/2026).");
                            break;
                        }

                        try {

                            if (unicenter.modificaAppello(appelloTrovato.getCodiceAppello(), nuovaDataOra, nuovaAula,
                                    nuoviPosti, nuovoVincolo, nuovoTermineIscrizione)) {
                                console.mostraMessaggio("Appello modificato con successo.");
                                break;
                            }
                        } catch (Exception e) {
                            console.mostraErrore(e.getMessage());
                            break;
                        }

                    }
                }
                case 4 -> {
                    console.mostraMessaggio("\n--- Sezione di eliminazione appelli ---");
                    console.mostraMessaggio("------------------------------------------");

                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();

                    console.mostraMessaggio("I tuoi appelli:");

                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        console.mostraMessaggio("Non hai appelli da eliminare.");
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
                        try {
                            if (unicenter.eliminaAppello(appelloTrovato.getCodiceAppello())) {
                                console.mostraMessaggio("Appello eliminato con successo.");
                                break;
                            } else {
                                console.mostraErrore("Errore durante l'eliminazione, riprovare.");
                                break;
                            }
                        } catch (Exception e) {
                            console.mostraErrore(e.getMessage());
                            break;
                        }
                    }
                }

                // ============================================================
                // UC3 - PUBBLICA ESITO ESAME
                // ============================================================
                case 5 -> {
                    console.mostraMessaggio("\n--- Pubblica Esito Esame ---");
                    console.mostraMessaggio("------------------------------------------");

                    // Mostra gli appelli del professore
                    List<Appello> appelliProfessore = unicenter.trovaAppelliProfessore();
                    if (appelliProfessore == null || appelliProfessore.isEmpty()) {
                        console.mostraMessaggio("Non hai appelli disponibili.");
                        break;
                    }
                    StampaAppelli(appelliProfessore);

                    String codAppello = console.leggiStringa("Inserisci il codice dell'appello: ");

                    // Verifica che l'appello appartenga al professore
                    boolean appelloValido = false;
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(codAppello)) {
                            appelloValido = true;
                            break;
                        }
                    }
                    if (!appelloValido) {
                        console.mostraErrore("Codice appello non valido.");
                        break;
                    }

                    // Mostra gli studenti iscritti, escludendo quelli con esito pendente per questa
                    // materia
                    List<Studente> tuttiIscritti = unicenter.trovaIscrittiByAppello(codAppello);
                    if (tuttiIscritti == null || tuttiIscritti.isEmpty()) {
                        console.mostraMessaggio("Non ci sono studenti iscritti a questo appello.");
                        break;
                    }

                    // Recupera il codice materia dell'appello per il filtro
                    String codiceMateriaAppello = null;
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(codAppello)) {
                            codiceMateriaAppello = a.getCodiceMateria();
                            break;
                        }
                    }

                    // Filtra: escludi studenti che hanno già un esito pendente per questa materia
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
                        console.mostraMessaggio(
                                "Tutti gli studenti iscritti hanno già un esito pendente per questa materia.");
                        break;
                    }
                    console.mostraMessaggio("\nStudenti iscritti (senza esito pendente):");
                    stampaStudenti(iscritti);

                    String matricola = console.leggiStringa("Inserisci la matricola dello studente: ");

                    // Verifica che lo studente sia iscritto (nella lista filtrata)
                    boolean studenteValido = false;
                    for (Studente s : iscritti) {
                        if (s.getMatricola().equals(matricola)) {
                            studenteValido = true;
                            break;
                        }
                    }
                    if (!studenteValido) {
                        console.mostraErrore("Matricola non trovata tra gli iscritti.");
                        break;
                    }

                    int voto = console.leggiIntero("Inserisci il voto (0-30): ");
                    boolean lode = false;
                    if (voto == 30) {
                        String lodeStr = console.leggiStringa("Lode? (s/n): ");
                        lode = lodeStr.equalsIgnoreCase("s");
                    }

                    try {
                        EsameSostenuto esito = unicenter.pubblicaEsitoEsame(
                                codAppello, matricola, codiceMateriaAppello,
                                voto, lode, 7 // 7 giorni di scadenza per la conferma
                        );
                        console.mostraMessaggio("Esito pubblicato con successo!");
                        console.mostraMessaggio("ID Esame: " + esito.getIdEsame());
                        console.mostraMessaggio("Stato: " + esito.getNomeStato());
                        if (esito.getNomeStato().equals("Bocciato")) {
                            console.mostraMessaggio("(Voto insufficiente - Regola di Dominio 4)");
                        } else {
                            console.mostraMessaggio(
                                    "Scadenza conferma: " + esito.getScadenzaConferma().format(formatterStampa));
                        }
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                    }
                }

                // ============================================================
                // UC3 - VISUALIZZA ESITI PUBBLICATI
                // ============================================================
                case 6 -> {
                    console.mostraMessaggio("\n--- Esiti Pubblicati ---");
                    List<EsameSostenuto> esitiProf = unicenter.getEsitiProfessore();
                    if (esitiProf == null || esitiProf.isEmpty()) {
                        console.mostraMessaggio("Non hai pubblicato nessun esito.");
                    } else {
                        stampaEsiti(esitiProf);
                    }
                }

                // ============================================================
                // UC7 - INVIA COMUNICAZIONE DI CORSO (Observer Pattern)
                // ============================================================
                case 7 -> {
                    console.mostraMessaggio("\n--- Invia Comunicazione di Corso (UC7) ---");
                    console.mostraMessaggio("Materie di cui sei professore:");
                    console.mostraMessaggio("------------------------------------------");
                    List<Materia> materieProf = unicenter.getMaterieDelProfessore();
                    if (materieProf == null || materieProf.isEmpty()) {
                        console.mostraMessaggio("Non sei abilitato a nessuna materia.");
                        break;
                    }
                    stampaMaterie(materieProf);

                    String codMateria = console.leggiStringa("Inserisci il codice della materia per l'avviso (0 per annullare): ");
                    if ("0".equals(codMateria)) {
                        break;
                    }

                    if (!unicenter.isProfessoreAbilitatoAMateria(codMateria)) {
                        console.mostraErrore("Non sei abilitato a gestire questa materia.");
                        break;
                    }

                    List<Studente> destinatari = unicenter.getStudentiDestinatariComunicazione(codMateria);
                    console.mostraMessaggio("Studenti attualmente iscritti alla materia (destinatari): " + destinatari.size());

                    String titolo = console.leggiStringa("Inserisci il titolo/oggetto dell'avviso: ");
                    String messaggio = console.leggiStringa("Inserisci il testo della comunicazione: ");

                    try {
                        int notificati = unicenter.inviaComunicazioneMateria(codMateria, titolo, messaggio);
                        console.mostraMessaggio("\n[SUCCESSO] Comunicazione pubblicata e inviata a " + notificati + " studente/i.");
                    } catch (Exception e) {
                        console.mostraErrore("Errore durante l'invio della comunicazione: " + e.getMessage());
                    }
                }

                case 0 -> back = true;
                default -> console.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }

    }

    // ==========================================
    // IMMATRICOLAZIONE
    // ==========================================
    private void gestisciImmatricolazione() {
        console.mostraMessaggio("\n------------------------------------------");
        console.mostraMessaggio("      IMMATRICOLAZIONE NUOVO STUDENTE     ");
        console.mostraMessaggio("------------------------------------------");

        try {
            if (unicenter.validaDataImmatricolazione()) {
                console.mostraMessaggio(
                        "Finestra temporale per l'immatricolazione aperta. Procedi con l'immatricolazione.");

                List<CorsoDiLaurea> corsi = unicenter.getCorsiDiLaureaAttivi();
                if (corsi == null || corsi.isEmpty()) {
                    console.mostraMessaggio("Nessun corso di laurea attivo disponibile al momento.");
                    return;
                }
                console.mostraMessaggio("Corsi di laurea disponibili (solo attivi):");
                stampaCorsiDiLaurea(corsi);
            }
        } catch (DataNonValidaException e) {
            console.mostraErrore(e.getMessage());
            return;
        }

        String nome = console.leggiStringa("Inserisci il nome dello studente: ");
        String cognome = console.leggiStringa("Inserisci il cognome dello studente: ");
        String email = console.leggiStringa("Inserisci l'email dello studente: ");
        String password = console.leggiStringa("Inserisci la password di almeno 4 caratteri: ");
        String corsoDiLaurea = console.leggiStringa("Inserisci il nome del corso di laurea: ");

        CorsoDiLaurea corso = unicenter.trovaCorsoDiLaureaByNome(corsoDiLaurea);

        if (corso == null) {
            console.mostraMessaggio("Corso di laurea non trovato.");
            return;
        }

        String codiceFiscale = console.leggiStringa("Inserisci il tuo codice fiscale : ");

        try {
            Studente nuovoStudente = unicenter.immatricolaStudente(nome, cognome, email, password, corsoDiLaurea,
                    codiceFiscale);

            console.mostraMessaggio("\nIMMATRICOLAZIONE AVVENUTA CON SUCCESSO!");
            console.mostraMessaggio("La tua matrricola è: " + nuovoStudente.getMatricola());
            console.mostraMessaggio("Tasse da pagare: " + nuovoStudente.getTasse());
            console.mostraMessaggio("Il tuo codice fiscale è: " + codiceFiscale);
        } catch (IllegalArgumentException e) {
            console.mostraErrore("immatricolazione fallita. " + e.getMessage());
        } catch (Exception e) {
            console.mostraErrore("immatricolazione fallita. " + e.getMessage());
        }
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
        } else if (unicenter.getCurrentUser() instanceof Amministratore) {
            menuAmministratore();
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
                            + ((appello.getVincoloLetteraCognome() == null
                                    || appello.getVincoloLetteraCognome().trim().isEmpty())
                                            ? "Nessuno"
                                            : appello.getVincoloLetteraCognome())
                            + "\n" +
                            "Data Termine Iscrizione: " + appello.getTermineIscrizione().format(formatterInputData)
                            + "\n" +
                            "----------------------------------------");
        }
    }

    public void stampaMaterie(List<Materia> materie) {
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
                    "Matricola: " + studente.getMatricola() + " | " +
                            studente.getNome() + " - " + studente.getCognome() + " - " + studente.getCodiceFiscale()
                            + "\n" +
                            "----------------------------------------");
        }
    }

    // =========================================================================
    // UC4/UC5 - MENU AREA AMMINISTRATORE
    // =========================================================================
    private void menuAmministratore() {
        boolean back = false;

        while (!back) {
            console.mostraMessaggio("\n------------------------------------------");
            console.mostraMessaggio("        AREA AMMINISTRATORE         ");
            console.mostraMessaggio("------------------------------------------");
            console.mostraMessaggio("1. Crea nuovo Corso di Laurea");
            console.mostraMessaggio("2. Rendi obsoleto un Corso di Laurea");
            console.mostraMessaggio("3. Visualizza tutti i Corsi di Laurea");
            console.mostraMessaggio("4. Crea nuova Materia (UC5)");
            console.mostraMessaggio("5. Visualizza tutte le Materie (UC5)");
            console.mostraMessaggio("6. Finalizza Corso di Laurea - associa materie (UC5)");
            console.mostraMessaggio("7. Associa Professore a Materia");
            console.mostraMessaggio("8. Elimina Corso di Laurea (Bozza non finalizzata o Obsoleto)");
            console.mostraMessaggio("0. Torna al menu principale");

            int scelta = console.leggiIntero("Seleziona un'opzione: ");

            switch (scelta) {
                // ============================================================
                // UC4 - CREA NUOVO CORSO DI LAUREA
                // ============================================================
                case 1 -> {
                    console.mostraMessaggio("\n--- Creazione Nuovo Corso di Laurea ---");
                    String nome = console.leggiStringa("Inserisci il nome del corso (es. Ingegneria Informatica): ");

                    console.mostraMessaggio("Tipologie disponibili:");
                    String[] tipologie = CorsoDiLaureaFactory.getTipologieValide();
                    for (int i = 0; i < tipologie.length; i++) {
                        int anniPrevisti = CorsoDiLaureaFactory.getAnniPerTipologia(tipologie[i]);
                        console.mostraMessaggio((i + 1) + ". " + tipologie[i] + " (" + anniPrevisti + " anni)");
                    }

                    int sceltaTipologia = console.leggiIntero("Seleziona la tipologia (1-" + tipologie.length + "): ");
                    if (sceltaTipologia < 1 || sceltaTipologia > tipologie.length) {
                        console.mostraErrore("Tipologia non valida.");
                        break;
                    }
                    String tipologia = tipologie[sceltaTipologia - 1];
                    int anniAccademici = CorsoDiLaureaFactory.getAnniPerTipologia(tipologia);

                    try {
                        CorsoDiLaurea nuovoCorso = unicenter.creaCorsoDiLaurea(nome, tipologia, anniAccademici);
                        console.mostraMessaggio("\nCorso di Laurea creato con successo!");
                        console.mostraMessaggio("Codice generato: " + nuovoCorso.getId());
                        console.mostraMessaggio("Nome: " + nuovoCorso.getNome());
                        console.mostraMessaggio("Tipologia: " + nuovoCorso.getTipologia());
                        console.mostraMessaggio("Durata: " + nuovoCorso.getAnniAccademici() + " anni");
                        console.mostraMessaggio(
                                "Stato: NON FINALIZZATO - Usa l'opzione 6 per associare le materie e finalizzare.");
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                    }
                }

                // ============================================================
                // UC4 - RENDI OBSOLETO CORSO DI LAUREA
                // ============================================================
                case 2 -> {
                    console.mostraMessaggio("\n--- Rendi Obsoleto Corso di Laurea ---");
                    List<CorsoDiLaurea> corsiAttivi = unicenter.getCorsiDiLaureaAttivi();
                    if (corsiAttivi == null || corsiAttivi.isEmpty()) {
                        console.mostraMessaggio("Nessun corso attivo da rendere obsoleto.");
                        break;
                    }
                    console.mostraMessaggio("Corsi attivi:");
                    stampaCorsiDiLaurea(corsiAttivi);

                    String codice = console
                            .leggiStringa("Inserisci il codice del corso da rendere obsoleto (0 per annullare): ");
                    if ("0".equals(codice))
                        break;

                    try {
                        unicenter.rendiObsoletoCorsoDiLaurea(codice);
                        console.mostraMessaggio(
                                "Corso reso obsoleto con successo. Non accetterà più nuove iscrizioni.");
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                    }
                }

                // ============================================================
                // UC4 - VISUALIZZA TUTTI I CORSI
                // ============================================================
                case 3 -> {
                    console.mostraMessaggio("\n--- Tutti i Corsi di Laurea ---");
                    List<CorsoDiLaurea> tuttiCorsi = unicenter.getCorsiDiLaurea();
                    if (tuttiCorsi == null || tuttiCorsi.isEmpty()) {
                        console.mostraMessaggio("Nessun corso di laurea presente nel sistema.");
                    } else {
                        stampaCorsiDiLaurea(tuttiCorsi);
                    }
                }

                // ============================================================
                // UC5 - CREA NUOVA MATERIA
                // ============================================================
                case 4 -> {
                    console.mostraMessaggio("\n--- Creazione Nuova Materia (UC5) ---");
                    String nomeMateria = console.leggiStringa("Inserisci il nome della materia: ");
                    int cfu = console.leggiIntero("Inserisci il numero di CFU: ");

                    try {
                        Materia nuovaMateria = unicenter.creaMateria(nomeMateria, cfu);
                        console.mostraMessaggio("\nMateria creata con successo!");
                        console.mostraMessaggio("Codice generato: " + nuovaMateria.getCodiceMateria());
                        console.mostraMessaggio("Nome: " + nuovaMateria.getNome());
                        console.mostraMessaggio("CFU: " + nuovaMateria.getCfu());
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                    }
                }

                // ============================================================
                // UC5 - VISUALIZZA TUTTE LE MATERIE
                // ============================================================
                case 5 -> {
                    console.mostraMessaggio("\n--- Tutte le Materie (UC5) ---");
                    List<Materia> tutteMaterie = unicenter.getTutteLeMaterie();
                    if (tutteMaterie == null || tutteMaterie.isEmpty()) {
                        console.mostraMessaggio("Nessuna materia presente nel sistema.");
                    } else {
                        for (Materia m : tutteMaterie) {
                            console.mostraMessaggio(
                                    "Codice: " + m.getCodiceMateria() + "\n" +
                                            "Nome: " + m.getNome() + "\n" +
                                            "CFU: " + m.getCfu() + "\n" +
                                            "----------------------------------------");
                        }
                        console.mostraMessaggio("Totale materie: " + tutteMaterie.size());
                    }
                }

                // ============================================================
                // UC5 - FINALIZZA CORSO DI LAUREA (ASSOCIA MATERIE)
                // ============================================================
                case 6 -> {
                    console.mostraMessaggio("\n--- Finalizza Corso di Laurea - Associa Materie (UC5) ---");

                    // 1. Mostra corsi non finalizzati
                    List<CorsoDiLaurea> corsiNonFinalizzati = unicenter.getCorsiNonFinalizzati();
                    if (corsiNonFinalizzati == null || corsiNonFinalizzati.isEmpty()) {
                        console.mostraMessaggio("Nessun corso non finalizzato disponibile.");
                        console.mostraMessaggio("Crea prima un nuovo corso di laurea (opzione 1).");
                        break;
                    }

                    console.mostraMessaggio("Corsi di Laurea NON ancora finalizzati:");
                    stampaCorsiDiLaurea(corsiNonFinalizzati);

                    String codiceCorso = console.leggiStringa(
                            "Inserisci il codice del corso da finalizzare (0 per annullare): ");
                    if ("0".equals(codiceCorso))
                        break;

                    // Verifica che il codice sia tra quelli non finalizzati
                    CorsoDiLaurea corsoScelto = null;
                    for (CorsoDiLaurea c : corsiNonFinalizzati) {
                        if (c.getId().equalsIgnoreCase(codiceCorso)) {
                            corsoScelto = c;
                            break;
                        }
                    }
                    if (corsoScelto == null) {
                        console.mostraErrore("Codice corso non valido o corso già finalizzato.");
                        break;
                    }

                    // 2. Mostra materie disponibili
                    List<Materia> materieDisponibili = unicenter.getTutteLeMaterie();
                    if (materieDisponibili == null || materieDisponibili.isEmpty()) {
                        console.mostraMessaggio("Nessuna materia disponibile nel sistema.");
                        console.mostraMessaggio("Crea prima le materie (opzione 4).");
                        break;
                    }

                    console.mostraMessaggio("\nCorso selezionato: " + corsoScelto.getNome()
                            + " (" + corsoScelto.getAnniAccademici() + " anni)");
                    console.mostraMessaggio("Materie disponibili:");
                    for (Materia m : materieDisponibili) {
                        console.mostraMessaggio(
                                "  " + m.getCodiceMateria() + " - " + m.getNome() + " (" + m.getCfu() + " CFU)");
                    }

                    // 3. Loop: associa materie con anno
                    boolean continua = true;
                    while (continua) {
                        console.mostraMessaggio("\n--- Associa una materia al corso ---");
                        String codiceMateria = console.leggiStringa(
                                "Inserisci il codice della materia da associare (0 per terminare e finalizzare): ");

                        if ("0".equals(codiceMateria)) {
                            continua = false;
                            break;
                        }

                        // Verifica che la materia esista
                        Materia materiaScelta = null;
                        for (Materia m : materieDisponibili) {
                            if (m.getCodiceMateria().equalsIgnoreCase(codiceMateria)) {
                                materiaScelta = m;
                                break;
                            }
                        }
                        if (materiaScelta == null) {
                            console.mostraErrore("Codice materia non valido. Riprova.");
                            continue;
                        }

                        int anno = console.leggiIntero(
                                "Inserisci l'anno accademico per '" + materiaScelta.getNome()
                                        + "' (1-" + corsoScelto.getAnniAccademici() + "): ");

                        try {
                            unicenter.associaMateriaACorso(corsoScelto.getId(), anno, materiaScelta);
                            console.mostraMessaggio("Materia '" + materiaScelta.getNome()
                                    + "' associata all'anno " + anno + " con successo!");
                        } catch (Exception e) {
                            console.mostraErrore(e.getMessage());
                        }
                    }

                    // 4. Riepilogo materie associate prima della finalizzazione
                    if (!corsoScelto.getMaterie().isEmpty()) {
                        console.mostraMessaggio("\n--- Riepilogo materie associate ---");
                        for (int anno = 1; anno <= corsoScelto.getAnniAccademici(); anno++) {
                            List<Materia> materieAnno = corsoScelto.getMaterieByAnno(anno);
                            if (!materieAnno.isEmpty()) {
                                console.mostraMessaggio("Anno " + anno + ":");
                                for (Materia m : materieAnno) {
                                    console.mostraMessaggio("  - " + m.getNome() + " (" + m.getCfu() + " CFU)");
                                }
                            }
                        }

                        String conferma = console.leggiStringa(
                                "\nConfermi la finalizzazione del corso? (s/n): ");
                        if (conferma.equalsIgnoreCase("s")) {
                            try {
                                unicenter.finalizzaCorso(corsoScelto.getId());
                                console.mostraMessaggio(
                                        "Corso '" + corsoScelto.getNome()
                                                + "' finalizzato con successo! Ora è visibile per l'immatricolazione.");
                            } catch (Exception e) {
                                console.mostraErrore(e.getMessage());
                            }
                        } else {
                            console.mostraMessaggio(
                                    "Finalizzazione annullata. Le materie associate sono state salvate, "
                                            + "potrai finalizzare in seguito.");
                        }
                    } else {
                        console.mostraMessaggio("Nessuna materia associata. Il corso non può essere finalizzato.");
                    }
                }

                // ============================================================
                // UC5 - ASSOCIA PROFESSORE A MATERIA
                // ============================================================
                case 7 -> {
                    console.mostraMessaggio("\n--- Associa Professore a Materia ---");

                    // 1. Mostra tutte le materie
                    List<Materia> tutteMaterie = unicenter.getTutteLeMaterie();
                    if (tutteMaterie == null || tutteMaterie.isEmpty()) {
                        console.mostraMessaggio("Nessuna materia presente nel sistema.");
                        break;
                    }

                    console.mostraMessaggio("Materie disponibili:");
                    for (Materia m : tutteMaterie) {
                        console.mostraMessaggio(
                                "  " + m.getCodiceMateria() + " - " + m.getNome() + " (" + m.getCfu() + " CFU)");
                    }

                    String codiceMateria = console.leggiStringa(
                            "Inserisci il codice della materia (0 per annullare): ");
                    if ("0".equals(codiceMateria))
                        break;

                    // Verifica che la materia esista
                    Materia materiaScelta = null;
                    for (Materia m : tutteMaterie) {
                        if (m.getCodiceMateria().equalsIgnoreCase(codiceMateria)) {
                            materiaScelta = m;
                            break;
                        }
                    }
                    if (materiaScelta == null) {
                        console.mostraErrore("Codice materia non valido.");
                        break;
                    }

                    // 2. Mostra solo i professori NON già associati a questa materia
                    List<Professore> professoriDisponibili = unicenter
                            .getProfessoriNonAssociatiAMateria(materiaScelta.getCodiceMateria());
                    if (professoriDisponibili == null || professoriDisponibili.isEmpty()) {
                        console.mostraMessaggio(
                                "Tutti i professori sono già associati alla materia '" + materiaScelta.getNome()
                                        + "'.");
                        break;
                    }

                    console.mostraMessaggio(
                            "\nProfessori disponibili per '" + materiaScelta.getNome() + "':");
                    for (Professore p : professoriDisponibili) {
                        console.mostraMessaggio(
                                "  ID: " + p.getIdProfessore() + " - " + p.getNome() + " " + p.getCognome());
                    }

                    String idProfessore = console.leggiStringa(
                            "Inserisci l'ID del professore da associare (0 per annullare): ");
                    if ("0".equals(idProfessore))
                        break;

                    // Verifica che il professore sia nella lista dei disponibili
                    boolean profValido = false;
                    for (Professore p : professoriDisponibili) {
                        if (p.getIdProfessore().equals(idProfessore)) {
                            profValido = true;
                            break;
                        }
                    }
                    if (!profValido) {
                        console.mostraErrore("ID professore non valido o già associato.");
                        break;
                    }

                    try {
                        unicenter.associaProfessoreAMateriaAdmin(idProfessore,
                                materiaScelta.getCodiceMateria());
                        console.mostraMessaggio("Professore associato con successo alla materia '"
                                + materiaScelta.getNome() + "'!");
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                    }
                }

                // ============================================================
                // UC4 - ELIMINA CORSO DI LAUREA (NON FINALIZZATO O OBSOLETO)
                // ============================================================
                case 8 -> {
                    console.mostraMessaggio("\n--- Elimina Corso di Laurea (Bozza o Obsoleto) ---");
                    List<CorsoDiLaurea> tuttiCorsi = unicenter.getCorsiDiLaurea();
                    List<CorsoDiLaurea> eliminabili = new java.util.ArrayList<>();
                    if (tuttiCorsi != null) {
                        for (CorsoDiLaurea c : tuttiCorsi) {
                            if (!c.isFinalizzato() || c.isObsoleto()) {
                                eliminabili.add(c);
                            }
                        }
                    }
                    if (eliminabili.isEmpty()) {
                        console.mostraMessaggio("Nessun corso di laurea eliminabile (solo corsi non finalizzati o obsoleti possono essere eliminati).");
                        break;
                    }
                    console.mostraMessaggio("Corsi di laurea eliminabili:");
                    stampaCorsiDiLaurea(eliminabili);

                    String codice = console.leggiStringa("Inserisci il codice del corso da eliminare (0 per annullare): ");
                    if ("0".equals(codice))
                        break;

                    try {
                        unicenter.eliminaCorsoDiLaurea(codice);
                        console.mostraMessaggio("Corso di laurea eliminato con successo!");
                    } catch (Exception e) {
                        console.mostraErrore(e.getMessage());
                    }
                }

                case 0 -> back = true;
                default -> console.mostraMessaggio("\nOpzione non valida. Riprova.");
            }
        }
    }

    public void stampaCorsiDiLaurea(List<CorsoDiLaurea> corsi) {
        for (CorsoDiLaurea corso : corsi) {
            String stato = corso.isObsoleto() ? "OBSOLETO" : "ATTIVO";
            String tipologia = corso.getTipologia() != null ? corso.getTipologia() : "N/D";
            String finalizzato = corso.isFinalizzato() ? "SI" : "NO";
            int numMaterie = corso.getMaterie().size();
            console.mostraMessaggio(
                    "Codice: " + corso.getId() + "\n" +
                            "Nome Corso: " + corso.getNome() + "\n" +
                            "Tipologia: " + tipologia + "\n" +
                            "Durata: " + corso.getAnniAccademici() + " anni\n" +
                            "Stato: " + stato + "\n" +
                            "Finalizzato: " + finalizzato + "\n" +
                            "Materie associate: " + numMaterie + "\n" +
                            "----------------------------------------");
        }
    }

    // =========================================================================
    // UC3 - METODO DI STAMPA ESITI
    // =========================================================================
    public void stampaEsiti(List<EsameSostenuto> esiti) {
        for (EsameSostenuto esame : esiti) {
            String scadenzaStr = esame.getNomeStato().equals("In attesa di conferma")
                    ? " | Scadenza: " + esame.getScadenzaConferma().format(formatterStampa)
                    : "";
            console.mostraMessaggio(
                    "ID: " + esame.getIdEsame()
                            + " | Materia: " + esame.getCodiceMateria()
                            + " | Voto: " + esame.getVotoNumerico()
                            + (esame.isLode() ? " e Lode" : "")
                            + " | Stato: " + esame.getNomeStato()
                            + scadenzaStr
                            + "\n----------------------------------------");
        }
    }

}
