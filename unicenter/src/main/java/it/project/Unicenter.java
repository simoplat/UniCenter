package it.project;

import it.project.controller.GestioneAppelliController;
import it.project.controller.IscrizioneAppelloController;
import it.project.controller.ImmatricolazioneController;
import it.project.notification.EmailServiceAdapter;
import it.project.notification.INotificaService;
import it.project.validation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Unicenter {

    private final List<Utente> utenti;
    private final List<Materia> materie;
    
    private final INotificaService notificaService;
    private final ImmatricolazioneController immatricolazioneController;
    private final GestioneAppelliController gestioneAppelliController;
    private final IscrizioneAppelloController iscrizioneAppelloController;

    ConsoleUI console = ConsoleUI.getInstance();

    private Unicenter() {
        this.utenti = new ArrayList<>();
        this.materie = new ArrayList<>();
        
        // Inizializzazione Adapter Notifiche
        this.notificaService = new EmailServiceAdapter();

        // 1. Inizializzazione Controller UC8 (Immatricolazione)
        this.immatricolazioneController = new ImmatricolazioneController();

        // 2. Inizializzazione Controller UC1 (Gestione Appelli)
        this.gestioneAppelliController = new GestioneAppelliController(this.notificaService);

        // 3. Inizializzazione Controller UC2 (Iscrizione Appelli con Chain of Responsibility)
        IscrizioneValidator catenaValidazione = new ValidationChainBuilder()
                .addValidator(new TassaPaidValidator())
                .addValidator(new PostiDisponibiliValidator())
                .addValidator(new CognomeFasciaValidator())
                .build();

        this.iscrizioneAppelloController = new IscrizioneAppelloController(this.notificaService, catenaValidazione);
    }

    private static class UnicenterHolder {
        private static final Unicenter INSTANCE = new Unicenter();
    }

    public static Unicenter getInstance() {
        return UnicenterHolder.INSTANCE;
    }


    public void start() {
        console.mostraMessaggio("=================================================================");
        console.mostraMessaggio("      BENVENUTO IN UNICENTER - GESTIONE UNIVERSITARIA           ");
        console.mostraMessaggio("=================================================================");
    }

    // =========================================================================
    // UC8: Iscriversi a Corso di Laurea (Immatricolazione)
    // =========================================================================
    public Studente immatricolaStudente(String nome, String cognome, String email, String corso, double tassaBase) {
        Studente nuovoStudente = immatricolazioneController.immatricolaStudente(nome, cognome, email, corso, tassaBase);
        utenti.add(nuovoStudente);
        console.mostraMessaggio("[UNICENTER] Immatricolato studente: " + nuovoStudente.getNome() + " " + nuovoStudente.getCognome() 
                + " con Matricola: " + nuovoStudente.getMatricola() + " - Tasse calcolate: €" + nuovoStudente.getTotaleTasse());
        return nuovoStudente;
    }



    // =========================================================================
    // UC1: Inserire Appello d'Esame
    // =========================================================================
    public Appello creaNuovoAppello(String codiceMateria, LocalDateTime dataOra, String aula, int posti, String vincoloCognome) {
        Materia materia = trovaMateria(codiceMateria)
                .orElseThrow(() -> new IllegalArgumentException("Materia non trovata: " + codiceMateria));

        // Recupera tutti gli studenti iscritti per notificarli dell'apertura
        List<Studente> studentiIscritti = getStudentiIscritti();

        Appello nuovoAppello = gestioneAppelliController.creaNuovoAppello(materia, dataOra, aula, posti, vincoloCognome, studentiIscritti);
        console.mostraMessaggio("[UNICENTER] Creato nuovo appello " + nuovoAppello.getCodiceAppello() + " per " + materia.getNome());
        return nuovoAppello;
    }

    // =========================================================================
    // UC2: Iscriversi ad un Appello d'Esame
    // =========================================================================
    public boolean iscriviStudenteAdAppello(String matricola, String codiceAppello) {
        Studente studente = trovaStudente(matricola)
                .orElseThrow(() -> new IllegalArgumentException("Studente non trovato: " + matricola));

        Appello appello = trovaAppello(codiceAppello)
                .orElseThrow(() -> new IllegalArgumentException("Appello non trovato: " + codiceAppello));

        return iscrizioneAppelloController.iscriviStudente(studente, appello);
    }

    // =========================================================================
    // Metodi di Utility e Ricerca
    // =========================================================================
    public void aggiungiMateria(Materia materia) {
        this.materie.add(materia);
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


    public void popolaDataBase(){

        Materia ingSoftware = new Materia("IS01", "Ingegneria del Software", 9);
        Materia basiDati = new Materia("BD01", "Basi di Dati", 6);
        
        this.aggiungiMateria(ingSoftware);
        this.aggiungiMateria(basiDati);

        
        // UC8: ISCRIVERSI A CORSO DI LAUREA (IMMATRICOLAZIONE);
        console.mostraMessaggio("IMMATRICOLAZIONE STUDENTI E CALCOLO TASSE");

        // Immatricolazione via StudenteBuilder, MatricolaGenerator e CalcoloTasseStrategy
        Studente mario = this.immatricolaStudente("Mario", "Rossi", "mario.rossi@studenti.it", "Ingegneria Informatica", 500.00);
        Studente luigi = this.immatricolaStudente("Luigi", "Verdi", "luigi.verdi@studenti.it", "Ingegneria Informatica", 500.00);
        Studente anna = this.immatricolaStudente("Anna", "Bianchi", "anna.bianchi@studenti.it", "Ingegneria Informatica", 500.00);

        // Configurazione Piani di Studi (Materia IS01 presente per Mario e Luigi, non per Anna)
        mario.getPianoStudi().aggiungiMateria("IS01");
        luigi.getPianoStudi().aggiungiMateria("IS01");
        // Anna non ha IS01 nel piano di studi per testare la fallibilità del PianoStudiValidator

        // Simulazione saldo tasse (solo Mario ha saldato le tasse universitarie)
        mario.setTassePagate(true);
        console.mostraMessaggio("--> Stato Tasse: Mario Rossi = PAGATO | Luigi Verdi = NON PAGATO | Anna Bianchi = NON PAGATO\n");


        // UC1: INSERIRE / AGGIORNARE APPELLO D'ESAME
        console.mostraMessaggio("CREAZIONE APPELLO D'ESAME CON NOTIFICA OBSERVER/ADAPTER");

        LocalDateTime dataEsame = LocalDateTime.now().plusDays(15);
        
        // Creazione appello via Factory Method (Materia) con 1 solo posto disponibile e vincolo fascia 'R-Z'
        Appello appelloIS = this.creaNuovoAppello("IS01", dataEsame, "Aula Magna", 1, "R-Z");
        console.mostraMessaggio("--> Appello generato con codice univoco: " + appelloIS.getCodiceAppello() + "\n");


        // UC2: ISCRIVERSI AD UN APPELLO D'ESAME (TEST CHAIN OF RESPONSIBILITY)
        console.mostraMessaggio("ISCRIZIONE APPELLI CON CATENA DI VALIDAZIONE (CHAIN OF RESP.)");

        // TEST 1: Mario Rossi (Piano Studi OK, Tasse OK, Posti OK, Iniziale Cognome 'R' OK)
        console.mostraMessaggio("\n[TEST 1] Tentativo iscrizione: Mario Rossi (" + mario.getMatricola() + ")");
        boolean esitoMario = this.iscriviStudenteAdAppello(mario.getMatricola(), appelloIS.getCodiceAppello());
        console.mostraMessaggio("ESITO: " + (esitoMario ? "SUCCESSO (Studente iscritto)" : "FALLITO"));

        // TEST 2: Luigi Verdi (Piano Studi OK, Tasse KO -> Fallirà su TassaPaidValidator)
        console.mostraMessaggio("\n[TEST 2] Tentativo iscrizione: Luigi Verdi (" + luigi.getMatricola() + ")");
        boolean esitoLuigi = this.iscriviStudenteAdAppello(luigi.getMatricola(), appelloIS.getCodiceAppello());
        console.mostraMessaggio("ESITO: " + (esitoLuigi ? "SUCCESSO (Studente iscritto)" : "FALLITO"));

        // Per il TEST 3: Paghiamo le tasse a Luigi per farlo avanzare nella catena
        luigi.setTassePagate(true);

        // TEST 3: Luigi Verdi ci riprova (Piano Studi OK, Tasse OK, Posti KO -> Fallirà su PostiDisponibiliValidator)
        console.mostraMessaggio("\n[TEST 3] Secondo tentativo iscrizione: Luigi Verdi (" + luigi.getMatricola() + ") dopo saldo tasse");
        boolean esitoLuigi2 = this.iscriviStudenteAdAppello(luigi.getMatricola(), appelloIS.getCodiceAppello());
        console.mostraMessaggio("ESITO: " + (esitoLuigi2 ? "SUCCESSO (Studente iscritto)" : "FALLITO"));

    }

}
    
