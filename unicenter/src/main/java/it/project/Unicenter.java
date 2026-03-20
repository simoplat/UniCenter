package it.project;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import it.project.exceptions.UtenteNonTrovatoException;

public class Unicenter {
    private Map<String, Studente> studenti;
    private Map<Integer, Professore> professori;
    private ConsoleUI consoleUi;
    private ControllerAppelli ctrlAppelli;
    private GestoreMaterie gestoreMaterie; 


    private static class UnicenterHolder {
        private static final Unicenter INSTANCE = new Unicenter();
    }

    public static Unicenter getInstance() {
        return UnicenterHolder.INSTANCE;
    }

    private Unicenter() {
        this.studenti = new HashMap<>();
        this.professori = new HashMap<>();
        this.consoleUi = ConsoleUI.getInstance();
        popolaDati();// SIMULAZIONE DI UN DATABASE
    }

    public void start() {
        consoleUi.mostraMessaggio("BENVENUTO IN UNICENTER");

        Utente utenteLoggato = null;

        while (utenteLoggato == null) {
            consoleUi.mostraMessaggio("Effettua il LOGIN:");
            String email = consoleUi.leggiStringa("Email: ");
            String password = consoleUi.leggiStringa("Password: ");

            try {
                utenteLoggato = autenticaUtente(email, password);
                consoleUi.mostraMessaggio("Login avvenuto con successo!");
            } catch (UtenteNonTrovatoException e) {
                consoleUi.mostraErrore(e.getMessage() + " Riprova.");
            }
        }
        utenteLoggato.menuPersonale();
    }

    private Utente autenticaUtente(String email, String password) throws UtenteNonTrovatoException {
        for (Professore p : professori.values()) {
            if (p.getEmail().equals(email) && p.getPassword().equals(password)) {
                return p;
            }
        }

        for (Studente s : studenti.values()) {
            if (s.getEmail().equals(email) && s.getPassword().equals(password)) {
                return s;
            }
        }

        throw new UtenteNonTrovatoException("Non è stato trovato nessun utente con queste credenziali");
    }

    private void popolaDati(){
        //SIMULAZIONE CARICAMENTO DA DATABASE
        //CONTROLLER
        Appello appelloFisica = new Appello(1, LocalDate.of(2026, 6, 15), "Aula Magna", LocalTime.of(9, 30), 150, 0, "Nessuno", 101);
        Appello appelloAnalisi = new Appello(2, LocalDate.of(2026, 7, 10), "Aula B1", LocalTime.of(14, 0), 50, 10, "Propedeuticità Matematica 0", 102);
        this.ctrlAppelli.aggiungiAppello(appelloAnalisi);
        this.ctrlAppelli = new ControllerAppelli();
        this.ctrlAppelli.aggiungiAppello(appelloFisica);
        //
        this.gestoreMaterie = new GestoreMaterie();

        //
        Professore p1 = new Professore(1, "Mario", "Rossi", "mario.rossi@uni.it", "123", "RSSMRA80A01H501U");
        Professore p2 = new Professore(2, "Elena", "Bianchi", "elena.bianchi@uni.it", "456", "BNCLNE85B41L219Z");

        professori.put(p1.getId(), p1);
        professori.put(p2.getId(), p2);

        Studente s1 = new Studente(null, 1, "Luca", "Verdi", "luca.verdi@stud.it", "student789", "VRDLCU02M10F205R");
        Studente s2 = new Studente(null, 2, "Giulia", "Neri", "giulia.neri@stud.it", "ghjkl321", "NREGLI03S50G273E");
        Carriera c1 = new Carriera(1, 200);
        Carriera c2 = new Carriera(1, 200);

        s1.setCarriera(c1);
        s2.setCarriera(c2);
        studenti.put(s1.getCodiceFiscale(), s1);
        studenti.put(s2.getCodiceFiscale(), s2);
    }

    protected void creaAppello(int idProfessore) throws UtenteNonTrovatoException {
        ctrlAppelli.trovaProfessore(idProfessore, professori);
        gestoreMaterie.trovaMaterieDiProfessore(idProfessore);
        
    }
}