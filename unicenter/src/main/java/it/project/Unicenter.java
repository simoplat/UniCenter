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
    private final GestioneCorsiLaureaController gestioneCorsiLaureaController;
    private final GestioneVotoController gestioneVotoController;
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
        this.gestioneCorsiLaureaController = new GestioneCorsiLaureaController();

        // 3. Inizializzazione Controller UC3 (Gestione Voto)
        this.gestioneVotoController = new GestioneVotoController(this, this.gestoreMaterie);

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
    public Studente immatricolaStudente(String nome, String cognome, String email, String password, String corso,
            String codiceFiscale) {
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

        // UC4: Blocca iscrizione a corsi obsoleti
        CorsoDiLaurea corsoTrovato = trovaCorsoDiLaureaByNome(corso);
        if (corsoTrovato != null && corsoTrovato.isObsoleto()) {
            throw new IllegalArgumentException(
                    "Impossibile immatricolarsi: il corso '" + corso + "' è obsoleto e non accetta nuove iscrizioni.");
        }

        // UC5: Blocca iscrizione a corsi non finalizzati
        if (corsoTrovato != null && !corsoTrovato.isFinalizzato()) {
            throw new IllegalArgumentException(
                    "Impossibile immatricolarsi: il corso '" + corso + "' non è ancora finalizzato.");
        }

        Studente nuovoStudente = immatricolazioneController.immatricolaStudente(nome, cognome, email, password, corso,
                codiceFiscale);
        utenti.add(nuovoStudente);

        // UC5: Auto-popola il piano di studi con le materie del corso
        if (corsoTrovato != null && corsoTrovato.isFinalizzato()) {
            PianoDiStudi pianoDiStudi = nuovoStudente.getPianoDiStudi();
            for (Materia materia : corsoTrovato.getMaterie()) {
                pianoDiStudi.aggiungiMateria(materia.getCodiceMateria());
            }
        }

        return nuovoStudente;
    }

    // Inserire Appello d'Esame
    public boolean creaNuovoAppello(String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili,
            String vincoloLetteraCognome, LocalDate termineIscrizione) throws Exception {
        return gestioneAppelliController.creaNuovoAppello(codiceMateria, dataOraStr, aula, postiDisponibili,
                vincoloLetteraCognome, termineIscrizione);
    }

    // iscriviStudenteAdAppello , iscrizione appello
    public List<Appello> trovaAppelliStudentePrenotabili() {

        if (!(this.currentUser instanceof Studente) || this.currentUser == null) {
            return Collections.emptyList();
        }

        Studente studente = (Studente) this.currentUser;
        PianoDiStudi pianoDiStudi = studente.getPianoDiStudi();
        if (pianoDiStudi == null || pianoDiStudi.getStato().equals("IN_ATTESA")) {
            console.mostraMessaggio(
                    "[UNICENTER] Impossibile iscrivere lo studente: il piano di studi non è approvato.");
            return Collections.emptyList();
        }
        return gestioneAppelliController.trovaAppelliPrenotabiliByStudente(studente, pianoDiStudi.getIdMaterie());
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

    public boolean iscriviStudenteAdAppello(String codiceAppello) throws Exception {
        return gestioneAppelliController.iscriviStudente((Studente) this.currentUser, codiceAppello);
    }

    public boolean disiscriviStudenteDaAppello(String codiceAppello) throws Exception {
        // Vincolo: la disiscrizione è bloccata se la data dell'esame è già passata
        Appello appello = gestioneAppelliController.trovaAppelloByIdAppello(codiceAppello);
        if (appello != null && appello.getDataOra().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Impossibile annullare la prenotazione: l'esame è già in corso o si è già svolto.");
        }
        return gestioneAppelliController.disiscriviStudente((Studente) this.currentUser, codiceAppello);
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

            // UC5: Creazione corsi con materie associate per anno (Creator GRASP)
            CorsoDiLaurea ingInformatica = new CorsoDiLaurea("ING-INF", "Ingegneria Informatica", 3);
            ingInformatica.aggiungiMateriaAdAnno(2, ingSoftware);   // Anno 2
            ingInformatica.aggiungiMateriaAdAnno(2, basiDati);      // Anno 2
            ingInformatica.aggiungiMateriaAdAnno(1, architetture);  // Anno 1
            ingInformatica.finalizza(); // Corso finalizzato: immutabile e visibile per immatricolazione

            // Corsi NON finalizzati (per testare il flusso UC5 dal menu admin)
            CorsoDiLaurea ingGestionale = new CorsoDiLaurea("ING-GES", "Ingegneria Gestionale", 3);
            CorsoDiLaurea ingElettronica = new CorsoDiLaurea("ING-ELE", "Ingegneria Elettronica", 3);
            CorsoDiLaurea ingMeccanica = new CorsoDiLaurea("ING-MEC", "Ingegneria Meccanica", 3);

            this.gestioneCorsiLaureaController.addCorsoDiLaurea(ingInformatica);
            this.gestioneCorsiLaureaController.addCorsoDiLaurea(ingGestionale);
            this.gestioneCorsiLaureaController.addCorsoDiLaurea(ingElettronica);
            this.gestioneCorsiLaureaController.addCorsoDiLaurea(ingMeccanica);

            // INSERIMENTO AMMINISTRATORE (UC4)
            Amministratore admin = new Amministratore(
                    "ADM-001", "Admin", "Sistema", "admin@unicenter.it", "admin123", "ADMSST80A01H501X");
            this.utenti.add(admin);
            console.mostraMessaggio("[DB POPULATION] Amministratore creato: " + admin.getEmail());

            // IMMATRICOLAZIONE STUDENTI (UC8 + Builder + Strategy + MatricolaGenerator)
            // UC5: il piano di studi viene auto-popolato con le materie del corso finalizzato

            // Studente 1: Mario Rossi (Tasse OK, Piano Studi auto-popolato da UC5)
            Studente st1 = this.immatricolaStudente("Mario", "Rossi", "mario.rossi@studenti.it", "pass123",
                    "Ingegneria Informatica", "CODICEFISCALEMARIOROSSI");
            st1.setTassePagate(true); // Tasse Saldate

            // Studente 2: Luigi Verdi (Tasse NON pagate, per testare i blocchi dei
            // validatori)
            Studente st2 = this.immatricolaStudente("Luigi", "Verdi", "luigi.verdi@studenti.it", "pass123",
                    "Ingegneria Informatica", "CODICEFISCALELUIGIVERDI");
            st2.setTassePagate(false);

            // Studente 3: Anna Bianchi (Piano di studi auto-popolato da UC5)
            Studente st3 = this.immatricolaStudente("Anna", "Bianchi", "anna.bianchi@studenti.it", "pass123",
                    "Ingegneria Informatica", "CODICEFISCALEANNABIANCHI");
            st3.setTassePagate(true);
            console.mostraMessaggio(st3.toString());

            Studente st4 = this.immatricolaStudente("Simo", "plata", "simo.plata@studenti.it", "pass123",
                    "Ingegneria Informatica", "SIMO");
            console.mostraMessaggio(st4.toString());

            // CREAZIONE APPELLI D'ESAME (UC1 + Factory Method + CodiceAppelloGenerator)
            LocalDateTime dataAppello1 = LocalDateTime.now().plusDays(10).withHour(9).withMinute(0);
            LocalDateTime dataAppello2 = LocalDateTime.now().plusDays(20).withHour(14).withMinute(30);

            // Appello 1: Ingegneria del Software (IS01) - 15 posti, fascia cognome R-Z
            // String codiceMateria, LocalDateTime dataOraStr, String aula, int
            // postiDisponibili, String vincoloLetteraCognome, LocalDate termineIscrizione
            this.gestioneAppelliController.creaNuovoAppello("IS01", dataAppello1, "Aula Magna", 15, "A-Z",
                    LocalDate.now().plusDays(10));

            this.gestioneAppelliController.creaNuovoAppello("BD01", dataAppello2, "Aula 101", 10, "A-Z",
                    LocalDate.now().plusDays(10));

            this.gestioneAppelliController.creaNuovoAppello("BD01", dataAppello2, "Aula 102", 20, "A-Z",
                    LocalDate.now().plusDays(10));

            this.gestioneAppelliController.iscriviStudente(st1, "APP-00001");

            Notifica notifica = new Notifica("Ciao", "ti sei iscritto", LocalDateTime.now());
            st1.aggiungiNotifica(notifica);

            // ===============================================================
            // UC3 - DATI DI TEST: Pubblicazione esiti esame
            // ===============================================================

            // Esito 1: Voto sufficiente per st1 su IS01 (In attesa di conferma)
            this.gestioneVotoController.pubblicaEsito(
                    "APP-00001", st1.getMatricola(), "IS01",
                    profRossi.getIdProfessore(), 28, false, 7);
            console.mostraMessaggio("[UC3 TEST] Pubblicato esito IS01 per " + st1.getMatricola() + ": 28/30");

            // Esito 2: Voto insufficiente per st2 su IS01 (Bocciato automaticamente - RD4)
            st2.setTassePagate(true); // Per permettere l'iscrizione di test
            this.gestioneAppelliController.iscriviStudente(st2, "APP-00001");
            this.gestioneVotoController.pubblicaEsito(
                    "APP-00001", st2.getMatricola(), "IS01",
                    profRossi.getIdProfessore(), 15, false, 7);
            console.mostraMessaggio(
                    "[UC3 TEST] Pubblicato esito IS01 per " + st2.getMatricola() + ": 15/30 (Bocciato)");

        } catch (DataNonValidaException e) {
            console.mostraMessaggio("[DB POPULATION ERROR] Errore durante il popolamento: " + e.getMessage());
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

    public Optional<Professore> trovaProfessore(String idProfessore) {
        return utenti.stream()
                .filter(u -> u instanceof Professore)
                .map(u -> (Professore) u)
                .filter(p -> p.getIdProfessore().equals(idProfessore))
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

    public boolean modificaAppello(String codiceAppello, LocalDateTime dataOra, String aula, int postiDisponibili,
            String vincolo, LocalDate dataTermineIscrizione) throws Exception {
        return gestioneAppelliController.modificaAppello(codiceAppello, dataOra, aula, postiDisponibili, vincolo,
                dataTermineIscrizione);
    }

    public boolean eliminaAppello(String codiceAppello) {
        return gestioneAppelliController.eliminaAppello(codiceAppello);
    }

    public CorsoDiLaurea trovaCorsoDiLaureaByNome(String nomeCorsoDiLaurea) {
        return gestioneCorsiLaureaController.trovaCorsoDiLaureaByNome(nomeCorsoDiLaurea);
    }

    public boolean validaDataImmatricolazione() throws DataNonValidaException {
        return immatricolazioneController.validaDataImmatricolazione();
    }

    public List<CorsoDiLaurea> getCorsiDiLaurea() {
        return gestioneCorsiLaureaController.getTuttiCorsi();
    }

    /**
     * Restituisce solo i corsi attivi (non obsoleti) — usato dall'immatricolazione.
     */
    public List<CorsoDiLaurea> getCorsiDiLaureaAttivi() {
        return gestioneCorsiLaureaController.getCorsiAttivi();
    }

    // =========================================================================
    // UC4 - FACADE METHODS (Gestione Corsi di Laurea)
    // =========================================================================

    /**
     * L'Amministratore crea un nuovo Corso di Laurea.
     */
    public CorsoDiLaurea creaCorsoDiLaurea(String nome, String tipologia, int anniAccademici) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può creare un corso di laurea.");
        }
        return gestioneCorsiLaureaController.creaCorsoDiLaurea(nome, tipologia, anniAccademici);
    }

    /**
     * L'Amministratore aggiorna un Corso di Laurea esistente.
     */
    public boolean aggiornaCorsoDiLaurea(String codice, String nuovoNome, String nuovaTipologia) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può aggiornare un corso di laurea.");
        }
        return gestioneCorsiLaureaController.aggiornaCorsoDiLaurea(codice, nuovoNome, nuovaTipologia);
    }

    /**
     * L'Amministratore rende obsoleto un Corso di Laurea (soft-delete).
     */
    public boolean rendiObsoletoCorsoDiLaurea(String codice) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può rendere obsoleto un corso di laurea.");
        }
        return gestioneCorsiLaureaController.rendiObsoletoCorsoDiLaurea(codice);
    }

    /**
     * Cerca un Corso di Laurea per codice.
     */
    public CorsoDiLaurea trovaCorsoDiLaureaByCodice(String codice) {
        return gestioneCorsiLaureaController.trovaCorsoDiLaureaByCodice(codice);
    }

    // =========================================================================
    // UC3 - FACADE METHODS (Gestione Voto)
    // =========================================================================

    /**
     * Il Professore pubblica l'esito di un esame.
     */
    public EsameSostenuto pubblicaEsitoEsame(String codiceAppello, String matricolaStudente,
            String codiceMateria, int votoNumerico,
            boolean lode, int giorniScadenza) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore può pubblicare un esito.");
        }
        Professore professore = (Professore) currentUser;
        return gestioneVotoController.pubblicaEsito(
                codiceAppello, matricolaStudente, codiceMateria,
                professore.getIdProfessore(), votoNumerico, lode, giorniScadenza);
    }

    /**
     * Lo Studente accetta il voto.
     */
    public boolean accettaVoto(String idEsame) {
        return gestioneVotoController.accettaVoto(idEsame);
    }

    /**
     * Lo Studente rifiuta il voto.
     */
    public boolean rifiutaVoto(String idEsame) {
        return gestioneVotoController.rifiutaVoto(idEsame);
    }

    /**
     * Verifica le scadenze per il silenzio-rifiuto (Estensione A).
     */
    public int verificaScadenzeVoti() {
        return gestioneVotoController.verificaScadenze();
    }

    /**
     * Restituisce gli esiti pendenti dello studente corrente.
     */
    public List<EsameSostenuto> getEsitiPendentiStudente() {
        if (!(currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        Studente studente = (Studente) currentUser;
        return gestioneVotoController.trovaEsitiPendentiByStudente(studente.getMatricola());
    }

    /**
     * Restituisce tutti gli esiti dello studente corrente (qualsiasi stato).
     */
    public List<EsameSostenuto> getTuttiEsitiStudente() {
        if (!(currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        Studente studente = (Studente) currentUser;
        return gestioneVotoController.trovaEsitiByStudente(studente.getMatricola());
    }

    /**
     * Restituisce tutti gli esiti pubblicati dal professore corrente.
     */
    public List<EsameSostenuto> getEsitiProfessore() {
        if (!(currentUser instanceof Professore)) {
            return Collections.emptyList();
        }
        Professore professore = (Professore) currentUser;
        return gestioneVotoController.trovaEsitiByProfessore(professore.getIdProfessore());
    }

    /**
     * Restituisce il libretto dello studente corrente.
     */
    public Libretto getLibrettoStudente() {
        if (!(currentUser instanceof Studente)) {
            return null;
        }
        return ((Studente) currentUser).getLibretto();
    }

    /**
     * Restituisce l'importo delle tasse dello studente corrente.
     */
    public double getTasseStudente() {
        if (currentUser instanceof Studente) {
            return ((Studente) currentUser).getTasse();
        }
        return 0.0;
    }

    /**
     * Verifica se le tasse dello studente corrente risultano saldate.
     */
    public boolean isTassePagateStudente() {
        if (currentUser instanceof Studente) {
            return ((Studente) currentUser).isTassePagate();
        }
        return false;
    }

    /**
     * Simula il pagamento delle tasse per lo studente corrente.
     */
    public boolean pagaTasseStudente() {
        if (currentUser instanceof Studente) {
            Studente studente = (Studente) currentUser;
            double importoPagato = studente.getTasse();
            studente.setTassePagate(true);
            studente.aggiungiNotifica(new Notifica(
                    "Pagamento Tasse",
                    "Hai saldato con successo le tasse universitarie di " + String.format("%.2f €", importoPagato) + ".",
                    LocalDateTime.now()
            ));
            return true;
        }
        return false;
    }

    public GestoreMaterieController getGestoreMaterie() {
        return gestoreMaterie;
    }

    /**
     * Restituisce gli esiti pendenti ("In attesa di conferma") per uno studente
     * dato la matricola.
     * Utilizzato dal GestioneAppelliController per impedire prenotazioni
     * ad appelli di materie con esiti ancora pendenti.
     *
     * @param matricola la matricola dello studente
     * @return lista degli esiti pendenti
     */
    public List<EsameSostenuto> getEsitiPendentiByMatricola(String matricola) {
        return gestioneVotoController.trovaEsitiPendentiByStudente(matricola);
    }

    // =========================================================================
    // UC5 - FACADE METHODS (Gestione Materie)
    // =========================================================================

    /**
     * L'Amministratore crea una nuova materia (nome, CFU).
     * Il codice viene generato automaticamente.
     */
    public Materia creaMateria(String nome, int cfu) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può creare una materia.");
        }
        return gestoreMaterie.creaMateria(nome, cfu);
    }

    /**
     * Restituisce tutte le materie create nel sistema.
     */
    public List<Materia> getTutteLeMaterie() {
        return gestoreMaterie.getTutteLeMaterie();
    }

    /**
     * Restituisce i corsi non ancora finalizzati (senza materie associate).
     */
    public List<CorsoDiLaurea> getCorsiNonFinalizzati() {
        return gestioneCorsiLaureaController.getCorsiNonFinalizzati();
    }

    /**
     * L'Amministratore associa una materia a un anno di un corso non finalizzato.
     */
    public void associaMateriaACorso(String codiceCorso, int anno, Materia materia) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può associare materie a un corso.");
        }
        gestioneCorsiLaureaController.associaMateriaACorso(codiceCorso, anno, materia);
    }

    /**
     * L'Amministratore finalizza un corso di laurea, rendendolo immutabile
     * e visibile per l'immatricolazione.
     */
    public void finalizzaCorso(String codiceCorso) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può finalizzare un corso.");
        }
        gestioneCorsiLaureaController.finalizzaCorso(codiceCorso);
    }

    /**
     * Restituisce tutti i professori registrati nel sistema.
     */
    public List<Professore> getTuttiProfessori() {
        List<Professore> professori = new ArrayList<>();
        for (Utente u : utenti) {
            if (u instanceof Professore) {
                professori.add((Professore) u);
            }
        }
        return professori;
    }

    /**
     * Restituisce i professori NON ancora associati a una materia.
     */
    public List<Professore> getProfessoriNonAssociatiAMateria(String codiceMateria) {
        List<String> idProfAssociati = gestoreMaterie.trovaProfessoriDellaMateria(codiceMateria);
        List<Professore> tuttiProf = getTuttiProfessori();
        List<Professore> nonAssociati = new ArrayList<>();
        for (Professore p : tuttiProf) {
            if (!idProfAssociati.contains(p.getIdProfessore())) {
                nonAssociati.add(p);
            }
        }
        return nonAssociati;
    }

    /**
     * L'Amministratore associa un professore a una materia.
     */
    public void associaProfessoreAMateriaAdmin(String idProfessore, String codiceMateria) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può associare un professore a una materia.");
        }
        gestoreMaterie.associaProfessoreAMateria(idProfessore, codiceMateria);
    }

}
