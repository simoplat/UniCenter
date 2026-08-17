package it.project.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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

                    // Aggiungere i messaggi di errore per i casi in cui l'iscrizione non va a buon
                    // fine

                    if (!unicenter.iscriviStudenteAdAppello(codiceAppello)) {
                        console.mostraMessaggio("Iscrizione non riuscita.");
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

                // ============================================================
                // UC3 - GESTIONE ESITI ESAMI (Accetta / Rifiuta voto)
                // ============================================================
                case 4 -> {
                    console.mostraMessaggio("\n--- Gestione Esiti Esami ---");

                    // Verifica scadenze (Estensione A: Silenzio Rifiuto)
                    int rifiutatiAuto = unicenter.verificaScadenzeVoti();
                    if (rifiutatiAuto > 0) {
                        console.mostraMessaggio("[SISTEMA] " + rifiutatiAuto + " esito/i rifiutato/i automaticamente per scadenza.");
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

                    String idEsame = console.leggiStringa("Inserisci l'ID dell'esame per cui vuoi esprimere la scelta (0 per uscire): ");
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
                            if (unicenter.accettaVoto(idEsame)) {
                                console.mostraMessaggio("Voto ACCETTATO con successo! Registrato nel libretto.");
                            } else {
                                console.mostraMessaggio("Impossibile accettare il voto.");
                            }
                        }
                        case 2 -> {
                            if (unicenter.rifiutaVoto(idEsame)) {
                                console.mostraMessaggio("Voto RIFIUTATO. Potrai iscriverti a un appello futuro.");
                            } else {
                                console.mostraMessaggio("Impossibile rifiutare il voto.");
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
                        console.mostraMessaggio(String.format("Media ponderata: %.2f/30", libretto.getMediaPonderata()));
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

                    // Mostra gli studenti iscritti
                    List<Studente> iscritti = unicenter.trovaIscrittiByAppello(codAppello);
                    if (iscritti == null || iscritti.isEmpty()) {
                        console.mostraMessaggio("Non ci sono studenti iscritti a questo appello.");
                        break;
                    }
                    console.mostraMessaggio("\nStudenti iscritti:");
                    stampaStudenti(iscritti);

                    String matricola = console.leggiStringa("Inserisci la matricola dello studente: ");

                    // Verifica che lo studente sia iscritto
                    boolean studenteValido = false;
                    String codiceMateriaPubblica = null;
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

                    // Recupera il codice materia dall'appello
                    for (Appello a : appelliProfessore) {
                        if (a.getCodiceAppello().equals(codAppello)) {
                            codiceMateriaPubblica = a.getCodiceMateria();
                            break;
                        }
                    }

                    int voto = console.leggiIntero("Inserisci il voto (0-30): ");
                    boolean lode = false;
                    if (voto == 30) {
                        String lodeStr = console.leggiStringa("Lode? (s/n): ");
                        lode = lodeStr.equalsIgnoreCase("s");
                    }

                    try {
                        EsameSostenuto esito = unicenter.pubblicaEsitoEsame(
                                codAppello, matricola, codiceMateriaPubblica,
                                voto, lode, 7 // 7 giorni di scadenza per la conferma
                        );
                        console.mostraMessaggio("Esito pubblicato con successo!");
                        console.mostraMessaggio("ID Esame: " + esito.getIdEsame());
                        console.mostraMessaggio("Stato: " + esito.getNomeStato());
                        if (esito.getNomeStato().equals("Bocciato")) {
                            console.mostraMessaggio("(Voto insufficiente - Regola di Dominio 4)");
                        } else {
                            console.mostraMessaggio("Scadenza conferma: " + esito.getScadenzaConferma().format(formatterStampa));
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
                
                List<CorsoDiLaurea> corsi = unicenter.getCorsiDiLaurea();
                if (corsi == null || corsi.isEmpty()) {
                    console.mostraMessaggio("Nessun corso di laurea disponibile al momento.");
                    return;
                }
                console.mostraMessaggio("Corsi di laurea disponibili:");
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
        String corsoDiLaurea = console.leggiStringa("Inserisci il corso di laurea : ");

        CorsoDiLaurea corso = unicenter.trovaCorsoDiLaureaByNome(corsoDiLaurea);

        if (corso == null) {
            console.mostraMessaggio("Corso di laurea non trovato.");
            return;
        }

        String codiceFiscale = console.leggiStringa("Inserisci il tuo codice fiscale : ");

        try {
            Studente nuovoStudente = unicenter.immatricolaStudente(nome, cognome, email, password, corsoDiLaurea, codiceFiscale);

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
                    studente.getNome() + " - " + studente.getCognome() + " - " + studente.getCodiceFiscale() + "\n" +
                            "----------------------------------------");
        }
    }

    public void stampaCorsiDiLaurea(List<CorsoDiLaurea> corsi) {
        for (CorsoDiLaurea corso : corsi) {
            console.mostraMessaggio(
                    "Nome Corso: " + corso.getNome() + "\n" +
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