package it.project;

import it.project.controller.*;
import it.project.validation.*;

import it.project.exceptions.UtenteNonTrovatoException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Unicenter {

    private final List<Utente> utenti;
    private final ImmatricolazioneController immatricolazioneController;
    private final GestioneAppelliController gestioneAppelliController;
    private final GestoreMaterie gestoreMaterie;
    private final MenuController menuController;
    private Utente currentUser = null;

    ConsoleUI console = ConsoleUI.getInstance();

    private Unicenter() {
        this.utenti = new ArrayList<>();
        this.menuController = new MenuController(this);

        // Inizializzazione Adapter Notifiche

        // 1. Inizializzazione Controller UC8 (Immatricolazione)
        this.immatricolazioneController = new ImmatricolazioneController();

        // 2. Inizializzazione Controller UC1 (Gestione Appelli)
        this.gestioneAppelliController = new GestioneAppelliController();

        this.gestoreMaterie = new GestoreMaterie();

        // 3. Inizializzazione Controller UC2 (Iscrizione Appelli con Chain of
        // Responsibility)
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
    public Studente immatricolaStudente(String nome, String cognome, String email, String corso, double tassaBase, String codiceFiscale) {
        Studente nuovoStudente = immatricolazioneController.immatricolaStudente(nome, cognome, email, corso, tassaBase, codiceFiscale);
        utenti.add(nuovoStudente);
        console.mostraMessaggio(
                "[UNICENTER] Immatricolato studente: " + nuovoStudente.getNome() + " " + nuovoStudente.getCognome()
                        + " con Matricola: " + nuovoStudente.getMatricola() + " - Tasse calcolate: €"
                        + nuovoStudente.getTotaleTasse());
        return nuovoStudente;
    }

    // Inserire Appello d'Esame
    public boolean creaNuovoAppello(Appello appello) throws Exception {
        return gestioneAppelliController.creaNuovoAppello(appello);
    }

    // iscriviStudenteAdAppello , iscrizione appello
    public List<Appello> trovaAppelliStudentePrenotabili() {
        Studente studente = (Studente) this.currentUser;

        PianoDiStudi pianoDiStudi = studente.getPianoStudi();
        if (pianoDiStudi == null || pianoDiStudi.getStato().equals("IN_ATTESA")) {
            console.mostraMessaggio(
                    "[UNICENTER] Impossibile iscrivere lo studente: il piano di studi non è approvato.");
            return null;
        }
        return gestioneAppelliController.trovaAppelliByIdMateria(pianoDiStudi.getCodiciMaterie());
    }

    public List <Appello> trovaAppelliPrenotatiDalloStudente (){
        Studente studente = (Studente) this.currentUser;
        return gestioneAppelliController.appelliPrenotatiByStudente(studente);
        
    }

    public List<Appello> trovaAppelliProfessore() {
        Professore professore = (Professore) currentUser;
        List<String> idMaterie = gestoreMaterie.trovaIdMaterieDiProfessore(professore.getIdProfessore());
        return gestioneAppelliController.trovaAppelliByIdMateria(idMaterie);
    }

    public List<Studente> trovaIscrittiByAppello(String codiceAppello){
       return gestioneAppelliController.trovaIscrittiByIdAppello(codiceAppello);
    }

    public boolean iscriviStudenteAdAppello(String codiceAppello) {
        if (gestioneAppelliController.iscriviStudente((Studente) this.currentUser, codiceAppello)) {
            return true;
        }
        return false;
    }

    public boolean disiscriviStudenteDaAppello(String codiceAppello) {
        if (gestioneAppelliController.disiscriviStudente((Studente) this.currentUser, codiceAppello)) {
            return true;
        }
        return false;
    }


    public void popolaDataBase() {
        try {
            console.mostraMessaggio("[DB POPULATION] Avvio popolamento dati di prova...");

            // INSERIMENTO MATERIE
            Materia ingSoftware = new Materia("IS01", "Ingegneria del Software", 9);
            Materia basiDati = new Materia("BD01", "Basi di Dati", 6);
            Materia architetture = new Materia("AR01", "Architettura dei Calcolatori", 6);
            this.gestoreMaterie.addMateria(ingSoftware);
            this.gestoreMaterie.addMateria(basiDati);
            this.gestoreMaterie.addMateria(architetture);

            // INSERIMENTO PROFESSORI
            Professore profRossi = new Professore(
                    "1", "Mario", "Rossi", "mario.rossi@unicenter.it", "pass123", "RSSMRA80A01H501U");
            Professore profVerdi = new Professore(
                    "2", "Giuseppe", "Verdi", "giuseppe.verdi@unicenter.it", "pass123", "VRDGPP75B02F205X");

            this.utenti.add(profRossi);
            this.utenti.add(profVerdi);
            this.gestoreMaterie.associaProfessoreAMateria("1", "IS01");
            this.gestoreMaterie.associaProfessoreAMateria("1", "BD01");
            this.gestoreMaterie.associaProfessoreAMateria("1", "AR01");
            this.gestoreMaterie.associaProfessoreAMateria("2", "AR01");
            this.gestoreMaterie.associaProfessoreAMateria("2", "IS01");

            // IMMATRICOLAZIONE STUDENTI (UC8 + Builder + Strategy + MatricolaGenerator)

            // Studente 1: Mario Rossi (Tasse OK, Piano Studi Completo)
            Studente st1 = this.immatricolaStudente("Mario", "Rossi", "mario.rossi@studenti.it",
                    "Ingegneria Informatica", 500.0, "CODICEFISCALEMARIOROSSI");
            st1.getPianoStudi().aggiungiMateria("IS01");
            st1.getPianoStudi().aggiungiMateria("BD01");
            st1.setTassePagate(true); // Tasse Saldate

            // Studente 2: Luigi Verdi (Tasse NON pagate, per testare i blocchi dei
            // validatori)
            Studente st2 = this.immatricolaStudente("Luigi", "Verdi", "luigi.verdi@studenti.it",
                    "Ingegneria Informatica", 500.0, "CODICEFISCALELUIGIVERDI");
            st2.getPianoStudi().aggiungiMateria("IS01");
            st2.setTassePagate(false); // Tasse NON Saldate

            // Studente 3: Anna Bianchi (Piano di studi limitato)
            Studente st3 = this.immatricolaStudente("Anna", "Bianchi", "anna.bianchi@studenti.it",
                    "Ingegneria Informatica", 500.0 , "CODICEFISCALEANNABIANCHI");
            st3.getPianoStudi().aggiungiMateria("BD01"); // Niente IS01 nel piano di studi
            st3.setTassePagate(true);

            Studente st4 = this.immatricolaStudente("Simo", "plata", "email", "ing.", 500, "SIMO");
            console.mostraMessaggio(st4.toString());

            // CREAZIONE APPELLI D'ESAME (UC1 + Factory Method + CodiceAppelloGenerator)
            LocalDateTime dataAppello1 = LocalDateTime.now().plusDays(10).withHour(9).withMinute(0);
            LocalDateTime dataAppello2 = LocalDateTime.now().plusDays(20).withHour(14).withMinute(30);

            // Appello 1: Ingegneria del Software (IS01) - 15 posti, fascia cognome R-Z
            Appello app1 = new Appello("APP1", "IS01", dataAppello1, "Aula Magna", 15, "A-Z");
            this.gestioneAppelliController.creaNuovoAppello(app1);

            Appello app2 = new Appello("APP2", "BD01", dataAppello2, "Aula 101", 10, "A-Z");
            this.gestioneAppelliController.creaNuovoAppello(app2);

            gestioneAppelliController.iscriviStudente(st1, "APP1");

            Notifica notifica = new Notifica("Ciao", "ti sei iscritto", LocalDateTime.now());
            st1.aggiungiNotifica(notifica);

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

    public Optional<Studente> trovaStudente(String matricola) {
        return utenti.stream()
                .filter(u -> u instanceof Studente)
                .map(u -> (Studente) u)
                .filter(s -> s.getMatricola().equalsIgnoreCase(matricola))
                .findFirst();
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

    public List<Materia> getMaterieDelProfessore() {
        Professore professore = (Professore) getCurrentUser();
        return gestoreMaterie.trovaMaterieDiProfessore(professore.getIdProfessore());
    }

    public boolean isProfessoreAbilitatoAMateria(String codiceMateria) {
        Professore professore = (Professore) getCurrentUser();
        return gestoreMaterie.isProfessoreAbilitatoAMateria(professore.getIdProfessore(), codiceMateria);
    }

    public String generaCodiceAppello() {
        return gestioneAppelliController.generaCodiceAppello();
    }

    public List<Notifica> getNotifichePerStudente() {
        if (currentUser instanceof Studente) {
            Studente studente = (Studente) currentUser;
            return studente.getNotifiche();
        }
        return null;
    }

    public boolean modificaAppello(String codiceAppello, LocalDateTime dataOra, String aula, int postiDisponibili, String vincolo){ 
        return gestioneAppelliController.modificaAppello(codiceAppello, dataOra, aula, postiDisponibili, vincolo);
    }

    public boolean eliminaAppello(String codiceAppello){
        return gestioneAppelliController.eliminaAppello(codiceAppello);        
    }

}
