package it.project;

import it.project.exceptions.UtenteNonTrovatoException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MenuController {
    // 1. Istanza statica privata per il Singleton
    private static MenuController instance;

    private final Unicenter unicenter;
    private final ConsoleUI ui;

    // 2. Costruttore PRIVATO
    private MenuController() {
        this.unicenter = Unicenter.getInstance(); 
        this.ui = ConsoleUI.getInstance(); 
    }

    // 3. Metodo statico pubblico per ottenere l'unica istanza
    public static synchronized MenuController getInstance() {
        if (instance == null) {
            instance = new MenuController();
        }
        return instance;
    }

    public void avvia() {
        // Popola il DB con i dati di test (UC8, UC1, UC2)[cite: 2]
        unicenter.popolaDataBase();

        ui.mostraIntestazione("BENVENUTO IN UNICENTER SYSTEM");

        boolean inEsecuzione = true;

        while (inEsecuzione) {
            ui.mostraMessaggio("\n--- ACCESSO UTENTE ---");
            ui.mostraMessaggio("(Digita 'esci' come email per chiudere il programma)");

            String email = ui.leggiStringa("Email: ");

            if (email.equalsIgnoreCase("esci")) {
                inEsecuzione = false;
                ui.mostraMessaggio("\nChiusura del sistema UniCenter in corso... Arrivederci!");
                break;
            }

            String password = ui.leggiStringa("Password: ");

            try {
                Utente utenteLoggato = unicenter.effettuaLogin(email, password);
                ui.mostraMessaggio("\n✅ Accesso effettuato! Benvenuto/a " + utenteLoggato.getNome() + " " + utenteLoggato.getCognome());

                // Smistamento in base al ruolo
                if (utenteLoggato instanceof Professore) {
                    menuProfessore((Professore) utenteLoggato);
                } else if (utenteLoggato instanceof Studente) {
                    menuStudente((Studente) utenteLoggato);
                }

            } catch (UtenteNonTrovatoException e) {
                ui.mostraErrore(e.getMessage());
                ui.mostraMessaggio("Verifica le credenziali e riprova.");
            }
        }
    }

    // =========================================================================
    // MENU PROFESSORE (UC1)[cite: 2]
    // =========================================================================
    private void menuProfessore(Professore prof) {
        boolean inSessione = true;

        while (inSessione) {
            ui.mostraIntestazione("AREA RISERVATA PROFESSORE: Prof. " + prof.getCognome());
            ui.mostraMessaggio("1. [UC1] Inserisci / Crea Nuovo Appello d'Esame");
            ui.mostraMessaggio("2. Visualizza Catalogo Materie e Appelli");
            ui.mostraMessaggio("0. Logout");

            String scelta = ui.leggiStringa("Seleziona un'opzione: ");

            switch (scelta) {
                case "1":
                    formCreazioneAppello();
                    break;
                case "2":
                    mostraCatalogo();
                    break;
                case "0":
                    inSessione = false;
                    ui.mostraMessaggio("Logout effettuato. Ritorno alla schermata di login.");
                    break;
                default:
                    ui.mostraErrore("Opzione non valida!");
            }
        }
    }

    // =========================================================================
    // MENU STUDENTE (UC2)[cite: 2]
    // =========================================================================
    private void menuStudente(Studente studente) {
        boolean inSessione = true;

        while (inSessione) {
            ui.mostraIntestazione("AREA RISERVATA STUDENTE: " + studente.getNome() + " " + studente.getCognome());
            ui.mostraMessaggio("Matricola: " + studente.getMatricola() + " | Corso: " + studente.getCorsoDiLaurea());
            ui.mostraMessaggio("1. [UC2] Iscriviti ad un Appello d'Esame");
            ui.mostraMessaggio("2. Visualizza Appelli Disponibili");
            ui.mostraMessaggio("3. Dettaglio Carriera e Stato Tasse");
            ui.mostraMessaggio("0. Logout");

            String scelta = ui.leggiStringa("Seleziona un'opzione: ");

            switch (scelta) {
                case "1":
                    formIscrizioneAppello(studente);
                    break;
                case "2":
                    mostraCatalogo();
                    break;
                case "3":
                    mostraDettaglioStudente(studente);
                    break;
                case "0":
                    inSessione = false;
                    ui.mostraMessaggio("Logout effettuato. Ritorno alla schermata di login.");
                    break;
                default:
                    ui.mostraErrore("Opzione non valida!");
            }
        }
    }

    // =========================================================================
    // FORM DI GESTIONE CASI D'USO
    // =========================================================================
    private void formCreazioneAppello() {
        ui.mostraMessaggio("\n--- [UC1] CREAZIONE NUOVO APPELLO D'ESAME ---");
        try {
            String codiceMateria = ui.leggiStringa("Codice Materia (es. IS01): ");
            String strDataOra = ui.leggiStringa("Data e Ora (formato: yyyy-MM-dd HH:mm): ");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dataOra = LocalDateTime.parse(strDataOra, formatter);

            String aula = ui.leggiStringa("Aula: ");
            int posti = ui.leggiIntero("Numero posti disponibili: ");
            String vincolo = ui.leggiStringa("Vincolo cognome (es. A-M, R-Z o premi Invio per nessuno): ");

            Appello appello = unicenter.creaNuovoAppello(codiceMateria, dataOra, aula, posti, vincolo);
            ui.mostraMessaggio("✅ Appello creato con successo! Codice univoco: " + appello.getCodiceAppello());

        } catch (DateTimeParseException e) {
            ui.mostraErrore("Formato data/ora non valido! Usa il formato yyyy-MM-dd HH:mm (es. 2026-09-20 09:00).");
        } catch (Exception e) {
            ui.mostraErrore("Errore durante la creazione dell'appello: " + e.getMessage());
        }
    }

    private void formIscrizioneAppello(Studente studente) {
        ui.mostraMessaggio("\n--- [UC2] ISCRIZIONE APPELLO D'ESAME ---");
        try {
            String codiceAppello = ui.leggiStringa("Inserisci Codice Appello (es. APP-00001): ");

            boolean esito = unicenter.iscriviStudenteAdAppello(studente.getMatricola(), codiceAppello);

            if (esito) {
                ui.mostraMessaggio("✅ Iscrizione avvenuta con successo!");
            } else {
                ui.mostraMessaggio("❌ Iscrizione respinta dai controlli di validazione.");
            }

        } catch (Exception e) {
            ui.mostraErrore("Errore durante l'iscrizione: " + e.getMessage());
        }
    }

    private void mostraCatalogo() {
        ui.mostraMessaggio("\n--- CATOLOGO MATERIE E APPELLI ---");
        unicenter.getMaterie().forEach(m -> {
            ui.mostraMessaggio("\n📘 [" + m.getCodiceMateria() + "] " + m.getNome());
            if (m.getAppelli().isEmpty()) {
                ui.mostraMessaggio("   └─ Nessun appello disponibile.");
            } else {
                m.getAppelli().forEach(a -> 
                    ui.mostraMessaggio("   └─ 📅 Appello [" + a.getCodiceAppello() + "] | Data: " + a.getDataOra() 
                            + " | Posti: " + a.getPostiDisponibili() + " | Vincolo: " 
                            + (a.getVincoloLetteraCognome().isEmpty() ? "Nessuno" : a.getVincoloLetteraCognome()))
                );
            }
        });
    }

    private void mostraDettaglioStudente(Studente studente) {
        ui.mostraMessaggio("\n--- DETTAGLIO CARRIERA STUDENTE ---");
        ui.mostraMessaggio("Nome completo: " + studente.getNome() + " " + studente.getCognome());
        ui.mostraMessaggio("Matricola: " + studente.getMatricola());
        ui.mostraMessaggio("Corso di Laurea: " + studente.getCorsoDiLaurea());
        ui.mostraMessaggio("Stato Tasse: " + (studente.isTassePagate() ? "PAGATE (€" + studente.getTotaleTasse() + ")" : "NON PAGATE"));
    }
}