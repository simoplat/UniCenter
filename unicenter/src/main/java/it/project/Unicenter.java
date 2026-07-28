package it.project;

import it.project.controller.GestioneAppelliController;
import it.project.controller.ImmatricolazioneController;
import it.project.notification.EmailServiceAdapter;
import it.project.notification.INotificaService;
import it.project.validation.*;

import it.project.exceptions.UtenteNonTrovatoException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Unicenter {

    private final List<Utente> utenti;
    private final List<Materia> materie;
    
    private final INotificaService notificaService;
    private final ImmatricolazioneController immatricolazioneController;
    private final GestioneAppelliController gestioneAppelliController;
    private final MenuController menuController;
    private Utente currentUser = null;

    ConsoleUI console = ConsoleUI.getInstance();

    private Unicenter() {
        this.utenti = new ArrayList<>();
        this.materie = new ArrayList<>();
        this.menuController = new MenuController(this);
        
        // Inizializzazione Adapter Notifiche
        this.notificaService = new EmailServiceAdapter();

        // 1. Inizializzazione Controller UC8 (Immatricolazione)
        this.immatricolazioneController = new ImmatricolazioneController();

        // 2. Inizializzazione Controller UC1 (Gestione Appelli)
        this.gestioneAppelliController = new GestioneAppelliController(this.notificaService, null);

        // 3. Inizializzazione Controller UC2 (Iscrizione Appelli con Chain of Responsibility)
        IscrizioneValidator catenaValidazione = new ValidationChainBuilder()
                .addValidator(new TassaPaidValidator())
                .addValidator(new PostiDisponibiliValidator())
                .addValidator(new CognomeFasciaValidator())
                .build();
    }

    private static class UnicenterHolder {
        private static final Unicenter INSTANCE = new Unicenter();
    }

    public static Unicenter getInstance() {
        return UnicenterHolder.INSTANCE;
    }


    public void avvia() {
        console.mostraMessaggio("[UNICENTER] Avvio del sistema UniCenter...");
        popolaDataBase();
        menuController.avvia();
    }

    // Immatricolazione
    public Studente immatricolaStudente(String nome, String cognome, String email, String corso, double tassaBase) {
        Studente nuovoStudente = immatricolazioneController.immatricolaStudente(nome, cognome, email, corso, tassaBase);
        utenti.add(nuovoStudente);
        console.mostraMessaggio("[UNICENTER] Immatricolato studente: " + nuovoStudente.getNome() + " " + nuovoStudente.getCognome() 
                + " con Matricola: " + nuovoStudente.getMatricola() + " - Tasse calcolate: €" + nuovoStudente.getTotaleTasse());
        return nuovoStudente;
    }

    // Inserire Appello d'Esame
    public Appello creaNuovoAppello(String codiceMateria, LocalDateTime dataOra, String aula, int posti, String vincoloCognome) {
        Materia materia = trovaMateria(codiceMateria)
                .orElseThrow(() -> new IllegalArgumentException("Materia non trovata: " + codiceMateria));

        // Recupera tutti gli studenti iscritti per notificarli dell'apertura
        List<Studente> studentiIscritti = getStudentiIscritti();

        Appello nuovoAppello = gestioneAppelliController.creaNuovoAppello(materia, dataOra, aula, posti, vincoloCognome, studentiIscritti);
        console.mostraMessaggio("[UNICENTER] Creato nuovo appello " + nuovoAppello.getCodiceAppello() + " per " + materia.getNome());
        return nuovoAppello;
    }

    // iscriviStudenteAdAppello , iscrizione appello
    public List<Appello> visualizzaAppelliDisponibili(){
        Studente studente = (Studente) this.currentUser;

        PianoDiStudi pianoDiStudi= studente.getPianoStudi();
        if (pianoDiStudi == null || pianoDiStudi.getStato().equals("IN_ATTESA")) {
            console.mostraMessaggio("[UNICENTER] Impossibile iscrivere lo studente: il piano di studi non è approvato.");
            return null;
        }
        return gestioneAppelliController.trovaAppelliDisponibili(pianoDiStudi.getCodiciMaterie());
    }
   
    public boolean iscriviStudenteAdAppello(String codiceAppello){
        if (gestioneAppelliController.iscriviStudente((Studente) this.currentUser,codiceAppello)) {
            return true;
        }
        return false;
    }

    public void aggiungiMateria(Materia materia) {
        this.materie.add(materia);
    }

public void popolaDataBase() {
    try {
        console.mostraMessaggio("[DB POPULATION] Avvio popolamento dati di prova...");

        // INSERIMENTO MATERIE
        Materia ingSoftware = new Materia("IS01", "Ingegneria del Software", 9);
        Materia basiDati = new Materia("BD01", "Basi di Dati", 6);
        Materia architetture = new Materia("AR01", "Architettura dei Calcolatori", 6);

        this.aggiungiMateria(ingSoftware);
        this.aggiungiMateria(basiDati);
        this.aggiungiMateria(architetture);

        // INSERIMENTO PROFESSORI
        Professore profRossi = new Professore(
                "1", "Mario", "Rossi", "mario.rossi@unicenter.it", "pass123", "RSSMRA80A01H501U"
        );
        Professore profVerdi = new Professore(
                "2", "Giuseppe", "Verdi", "giuseppe.verdi@unicenter.it", "pass123", "VRDGPP75B02F205X"
        );

        this.utenti.add(profRossi);
        this.utenti.add(profVerdi);

        // IMMATRICOLAZIONE STUDENTI (UC8 + Builder + Strategy + MatricolaGenerator)

        // Studente 1: Mario Rossi (Tasse OK, Piano Studi Completo)
        Studente st1 = this.immatricolaStudente("Mario", "Rossi", "mario.rossi@studenti.it", "Ingegneria Informatica", 500.0);
        st1.getPianoStudi().aggiungiMateria("IS01");
        st1.getPianoStudi().aggiungiMateria("BD01");
        st1.setTassePagate(true); // Tasse Saldate

        // Studente 2: Luigi Verdi (Tasse NON pagate, per testare i blocchi dei validatori)
        Studente st2 = this.immatricolaStudente("Luigi", "Verdi", "luigi.verdi@studenti.it", "Ingegneria Informatica", 500.0);
        st2.getPianoStudi().aggiungiMateria("IS01");
        st2.setTassePagate(false); // Tasse NON Saldate

        // Studente 3: Anna Bianchi (Piano di studi limitato)
        Studente st3 = this.immatricolaStudente("Anna", "Bianchi", "anna.bianchi@studenti.it", "Ingegneria Informatica", 500.0);
        st3.getPianoStudi().aggiungiMateria("BD01"); // Niente IS01 nel piano di studi
        st3.setTassePagate(true);

        // CREAZIONE APPELLI D'ESAME (UC1 + Factory Method + CodiceAppelloGenerator)
        LocalDateTime dataAppello1 = LocalDateTime.now().plusDays(10).withHour(9).withMinute(0);
        LocalDateTime dataAppello2 = LocalDateTime.now().plusDays(20).withHour(14).withMinute(30);

        // Appello 1: Ingegneria del Software (IS01) - 15 posti, fascia cognome R-Z
        Appello app1 = this.creaNuovoAppello("IS01", dataAppello1, "Aula Magna", 15, "R-Z");

        // Appello 2: Basi di Dati (BD01) - 1 solo posto (per testare PostiDisponibiliValidator)
        Appello app2 = this.creaNuovoAppello("BD01", dataAppello2, "Lab Informatica 2", 1, "A-Z");

        console.mostraMessaggio("[DB POPULATION] Popolamento completato con successo!");
        console.mostraMessaggio("  - Materie caricate: " + materie.size());
        console.mostraMessaggio("  - Utenti caricati: " + utenti.size() + " (3 Studenti, 2 Professori)");
        console.mostraMessaggio("  - Appelli generati: " + app1.getCodiceAppello() + " (" + app1.getCodiceMateria() + "), " 
                           + app2.getCodiceAppello() + " (" + app2.getCodiceMateria() + ")");
        console.mostraMessaggio("-----------------------------------------------------------------\n");

    } catch (Exception e) {
        console.mostraMessaggio("[DB POPULATION ERROR] Errore durante il popolamento: " + e.getMessage());
    }
}

public Utente effettuaLogin(String email, String password) throws UtenteNonTrovatoException {
    for (Utente u : utenti) {
        if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
            return u;
        }
    }
    throw new UtenteNonTrovatoException("Credenziali non valide: email o password errati.");
}

public List<Materia> getMaterie() {
    return Collections.unmodifiableList(materie);
}

    public Optional<Materia> trovaMateria(String codiceMateria) {
        return materie.stream()
                .filter(m -> m.getCodiceMateria().equalsIgnoreCase(codiceMateria))
                .findFirst();
    }

    public Optional<Studente> trovaStudente(String matricola) {
        return utenti.stream()
                .filter(u -> u instanceof Studente)
                .map(u -> (Studente) u)
                .filter(s -> s.getMatricola().equalsIgnoreCase(matricola))
                .findFirst();
    }

    public Optional<Appello> trovaAppello(String codiceAppello) {
        for (Materia m : materie) {
            for (Appello a : m.getAppelli()) {
                if (a.getCodiceAppello().equalsIgnoreCase(codiceAppello)) {
                    return Optional.of(a);
                }
            }
        }
        return Optional.empty();
    }



    public List<Studente> getStudentiIscritti() {
        List<Studente> studenti = new ArrayList<>();
        for (Utente u : utenti) {
            if (u instanceof Studente) {
                studenti.add((Studente) u);
            }
        }
        return studenti;
    }



    public boolean esisteUtente(String email) {
        for (Utente u : utenti) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public boolean passwordCorretta(String email, String password) {
        for (Utente u : utenti) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                this.currentUser = u; // Imposta l'utente corrente dopo il login
                return true;
            }
        }
        return false;

    }

    public Utente getCurrentUser() {
        return currentUser;
    }

}
    
