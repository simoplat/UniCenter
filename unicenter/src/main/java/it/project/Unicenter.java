package it.project;

import java.util.HashMap;
import java.util.Map;

import it.project.exceptions.UtenteNonTrovatoException;

public class Unicenter {
    private Map<String, Studente> studenti;
    private Map<String, Professore> professori;
    private Map<String, Materia> materie;
    private Map<String, Appello> appelli;
    private ConsoleUI consoleUi;

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

        //SIMULAZIONE CARICAMENTO DA DATABASE
        Professore p1 = new Professore(1, "Mario", "Rossi", "mario.rossi@uni.it", "123", "RSSMRA80A01H501U");
        Professore p2 = new Professore(2, "Elena", "Bianchi", "elena.bianchi@uni.it", "456", "BNCLNE85B41L219Z");

        professori.put(p1.getCodiceFiscale(), p1);
        professori.put(p2.getCodiceFiscale(), p2);

        Studente s1 = new Studente(null, 1, "Luca", "Verdi", "luca.verdi@stud.it", "student789", "VRDLCU02M10F205R");
        Studente s2 = new Studente(null, 2, "Giulia", "Neri", "giulia.neri@stud.it", "ghjkl321", "NREGLI03S50G273E");

        studenti.put(s1.getCodiceFiscale(), s1);
        studenti.put(s2.getCodiceFiscale(), s2);   
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

    public void immatricola(Studente studente){

    }

}