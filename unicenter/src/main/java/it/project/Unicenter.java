package it.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import it.project.controller.*;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.UtenteNonTrovatoException;

public class Unicenter {
    private final List<Utente> utenti;
    private final ImmatricolazioneController immatricolazioneController;
    private final GestioneAppelliController gestioneAppelliController;
    private final GestoreMaterieController gestoreMaterie;
    private final CorsoDiLaureaController corsoDiLaureaController;
    private final MenuController menuController;
    private Utente currentUser = null;

    ConsoleUI console = ConsoleUI.getInstance();

    private Unicenter() {
        this.utenti = new ArrayList<>();
        this.menuController = new MenuController(this);

        // Inizializzazione Adapter Notifiche

        // 1. Inizializzazione Controller UC8 (Immatricolazione)
        this.immatricolazioneController = new ImmatricolazioneController(this);

        // 2. Inizializzazione Controller UC1 (Gestione Appelli)
        this.gestioneAppelliController = new GestioneAppelliController(this);

        this.gestoreMaterie = new GestoreMaterieController();
        this.corsoDiLaureaController = new CorsoDiLaureaController(this);

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
    public Studente immatricolaStudente(String nome, String cognome, String email, String password, String corso, double tassaBase, String codiceFiscale) {
        boolean emailEsiste = esisteUtente(email);
        boolean cfEsiste = esisteCodiceFiscale(codiceFiscale);

        // controllo 3 casistiche
        if (emailEsiste && cfEsiste) {
            throw new IllegalArgumentException("Attenzione: Email e Codice Fiscale già inseriti!");
        } else if (emailEsiste) {
            throw new IllegalArgumentException("Attenzione: Email già inserita!");
        } else if (cfEsiste) {
            throw new IllegalArgumentException("Attenzione: Codice Fiscale già inserito!");
        }

        Studente nuovoStudente = immatricolazioneController.immatricolaStudente(nome, cognome, email, password, corso, tassaBase, codiceFiscale);
        utenti.add(nuovoStudente);
        return nuovoStudente;
    }

    // Inserire Appello d'Esame
    public boolean creaNuovoAppello(String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili, String vincoloLetteraCognome, LocalDate termineIscrizione) throws Exception {
        return gestioneAppelliController.creaNuovoAppello(codiceMateria, dataOraStr, aula, postiDisponibili, vincoloLetteraCognome, termineIscrizione);
    }

    // iscriviStudenteAdAppello , iscrizione appello
    public List<Appello> trovaAppelliStudentePrenotabili() {
        
        if (!(this.currentUser instanceof Studente) || this.currentUser == null) {
            return Collections.emptyList();
        }

        Studente studente = (Studente) this.currentUser;
        PianoDiStudi pianoDiStudi = studente.getPianoStudi();
        if (pianoDiStudi == null || pianoDiStudi.getStato().equals("IN_ATTESA")) {
            console.mostraMessaggio(
                    "[UNICENTER] Impossibile iscrivere lo studente: il piano di studi non è approvato.");
            return Collections.emptyList();
        }
        return gestioneAppelliController.trovaAppelliPrenotabiliByStudente(studente, pianoDiStudi.getCodiciMaterie());
    }

    public List<Appello> trovaAppelliPrenotatiDalloStudente() {
        if (!(this.currentUser instanceof Studente) || this.currentUser == null) {
            return Collections.emptyList();
        }
        Studente studente = (Studente) this.currentUser;
        return gestioneAppelliController.appelliPrenotatiByStudente(studente);

    }

    public List<Appello> trovaAppelliProfessore() {
        if (!(this.currentUser instanceof Professore) || this.currentUser == null) {
            return Collections.emptyList();
        }
        Professore professore = (Professore) currentUser;
        List<String> idMaterie = gestoreMaterie.trovaIdMaterieDiProfessore(professore.getIdProfessore());
        return gestioneAppelliController.trovaAppelliByIdMateria(idMaterie);
    }

    public List<Studente> trovaIscrittiByAppello(String codiceAppello) {
        
        return gestioneAppelliController.trovaIscrittiByIdAppello(codiceAppello);
    }

    public boolean iscriviStudenteAdAppello(String codiceAppello) {
        return gestioneAppelliController.iscriviStudente((Studente) this.currentUser, codiceAppello);
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
                    "2", "Giuseppe", "Verdi", "giuseppeverdi@unicenter.it", "pass123", "VRDGPP75B02F205X");

            this.utenti.add(profRossi);
            this.utenti.add(profVerdi);
            this.gestoreMaterie.associaProfessoreAMateria("1", "IS01");
            this.gestoreMaterie.associaProfessoreAMateria("1", "BD01");
            this.gestoreMaterie.associaProfessoreAMateria("1", "AR01");
            this.gestoreMaterie.associaProfessoreAMateria("2", "AR01");
            this.gestoreMaterie.associaProfessoreAMateria("2", "IS01");

            CorsoDiLaurea ingInformatica = new CorsoDiLaurea("ING-INF", "Ingegneria Informatica");
            ingInformatica.aggiungiMateria(ingSoftware);
            ingInformatica.aggiungiMateria(basiDati);
            ingInformatica.aggiungiMateria(architetture);
            this.corsoDiLaureaController.addCorsoDiLaurea(ingInformatica);

            // IMMATRICOLAZIONE STUDENTI (UC8 + Builder + Strategy + MatricolaGenerator)

            // Studente 1: Mario Rossi (Tasse OK, Piano Studi Completo)
            Studente st1 = this.immatricolaStudente("Mario", "Rossi", "mario.rossi@studenti.it", "pass123",
                    "Ingegneria Informatica", 500.0, "CODICEFISCALEMARIOROSSI");
            st1.getPianoStudi().aggiungiMateria("IS01");
            st1.getPianoStudi().aggiungiMateria("BD01");
            st1.setTassePagate(true); // Tasse Saldate

            // Studente 2: Luigi Verdi (Tasse NON pagate, per testare i blocchi dei
            // validatori)
            Studente st2 = this.immatricolaStudente("Luigi", "Verdi", "luigi.verdi@studenti.it", "pass123",
                    "Ingegneria Informatica", 500.0, "CODICEFISCALELUIGIVERDI");
            st2.getPianoStudi().aggiungiMateria("IS01");
            st2.setTassePagate(false);

            // Studente 3: Anna Bianchi (Piano di studi limitato)
            Studente st3 = this.immatricolaStudente("Anna", "Bianchi", "anna.bianchi@studenti.it", "pass123",
                    "Ingegneria Informatica", 500.0, "CODICEFISCALEANNABIANCHI");
            st3.getPianoStudi().aggiungiMateria("BD01"); // Niente IS01 nel piano di studi
            st3.setTassePagate(true);
            console.mostraMessaggio(st3.toString());


            Studente st4 = this.immatricolaStudente("Simo", "plata", "simo.plata@studenti.it", "pass123",
                    "Ingegneria Informatica", 500, "SIMO");
            console.mostraMessaggio(st4.toString());

            // CREAZIONE APPELLI D'ESAME (UC1 + Factory Method + CodiceAppelloGenerator)
            LocalDateTime dataAppello1 = LocalDateTime.now().plusDays(10).withHour(9).withMinute(0);
            LocalDateTime dataAppello2 = LocalDateTime.now().plusDays(20).withHour(14).withMinute(30);

            // Appello 1: Ingegneria del Software (IS01) - 15 posti, fascia cognome R-Z
            //String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili, String vincoloLetteraCognome, LocalDate termineIscrizione
            this.gestioneAppelliController.creaNuovoAppello("IS01", dataAppello1, "Aula Magna", 15, "A-Z", LocalDate.now().plusDays(10));

            this.gestioneAppelliController.creaNuovoAppello("BD01", dataAppello2, "Aula 101", 10, "A-Z", LocalDate.now().plusDays(10));

            this.gestioneAppelliController.creaNuovoAppello("BD01", dataAppello2, "Aula 102", 20, "A-Z", LocalDate.now().plusDays(10));

            this.gestioneAppelliController.iscriviStudente(st1, "APP-00001");

            Notifica notifica = new Notifica("Ciao", "ti sei iscritto", LocalDateTime.now());
            st1.aggiungiNotifica(notifica);

        
        } 
        catch (DataNonValidaException e) {
            console.mostraMessaggio("[DB POPULATION ERROR] Errore durante il popolamento: " + e.getMessage());
        }
        catch (Exception e) {
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

    public boolean esisteCodiceFiscale(String codiceFiscale) {
        for (Utente u : utenti) {
            if (u.getCodiceFiscale().equalsIgnoreCase(codiceFiscale)) {
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
        return Collections.emptyList();
    }

    public boolean modificaAppello(String codiceAppello, LocalDateTime dataOra, String aula, int postiDisponibili, String vincolo, LocalDate dataTermineIscrizione) throws Exception {
        return gestioneAppelliController.modificaAppello(codiceAppello, dataOra, aula, postiDisponibili, vincolo, dataTermineIscrizione);
    }

    public boolean eliminaAppello(String codiceAppello) {
        return gestioneAppelliController.eliminaAppello(codiceAppello);
    }

    public CorsoDiLaurea trovaCorsoDiLaureaByNome(String nomeCorsoDiLaurea) {
        return corsoDiLaureaController.trovaCorsoDiLaureaByNome(nomeCorsoDiLaurea);
    }

    public boolean validaDataImmatricolazione() throws DataNonValidaException {
        return immatricolazioneController.validaDataImmatricolazione();
    }

}
