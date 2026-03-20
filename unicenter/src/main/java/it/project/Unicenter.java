package it.project;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.project.exceptions.UtenteNonTrovatoException;

public class Unicenter {
    private Map<String, Studente> studenti;
    private Map<Integer, Professore> professori;
    private ConsoleUI consoleUi;
    private ControllerAppelli ctrlAppelli;
    private GestoreMaterie gestoreMaterie; 
    private Map<String, Materia> materie;


    private static class UnicenterHolder {
        private static final Unicenter INSTANCE = new Unicenter();
    }

    public static Unicenter getInstance() {
        return UnicenterHolder.INSTANCE;
    }

    private Unicenter() {
        this.studenti = new HashMap<>();
        this.professori = new HashMap<>();
        this.materie = new HashMap<>();
        this.consoleUi = ConsoleUI.getInstance();
        this.ctrlAppelli = new ControllerAppelli(new HashMap<>());
        this.gestoreMaterie = new GestoreMaterie(this.materie, new HashMap<>(), new HashMap<>());
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

        throw new UtenteNonTrovatoException("Credenziali errate.");
    }

    private void popolaDati(){
        // 1. Creazione Materie
        Materia m1 = new Materia(101, 6, "Fisica");
        Materia m2 = new Materia(102, 9, "Analisi Matematica");
        materie.put("101", m1);
        materie.put("102", m2);

        // 2. Creazione Professori
        Professore p1 = new Professore(1, "Mario", "Rossi", "mario.rossi@uni.it", "123", "RSSMRA80A01H501U");
        Professore p2 = new Professore(2, "Elena", "Bianchi", "elena.bianchi@uni.it", "456", "BNCLNE85B41L219Z");
        professori.put(p1.getId(), p1);
        professori.put(p2.getId(), p2);

        // 3. Associazioni Professore-Materia
        gestoreMaterie.associaProfessoreAMateria(1, 101); // Rossi -> Fisica
        gestoreMaterie.associaProfessoreAMateria(2, 102); // Bianchi -> Analisi

        // 4. Creazione Studenti e Carriere
        Studente s1 = new Studente(null, 10, "Luca", "Verdi", "luca.verdi@stud.it", "stud1", "VRDLCU02M10F205R");
        s1.setCarriera(new Carriera(1001, 250.0));
        studenti.put(s1.getCodiceFiscale(), s1);

        // 5. Appelli esistenti
        ctrlAppelli.aggiungiAppello(new Appello(1, LocalDate.of(2026, 6, 15), "Aula Magna", LocalTime.of(9, 30), 150, 0, "Nessuno", 101));
    }

    protected void creaAppello(int idProfessore) throws UtenteNonTrovatoException {
        ctrlAppelli.trovaProfessore(idProfessore, professori);
        List <Materia> listaMaterie = gestoreMaterie.trovaMaterieDiProfessore(idProfessore);
        for (Materia materia : listaMaterie) {
            consoleUi.mostraMessaggio(materia.toString());
        }
        
    }
}