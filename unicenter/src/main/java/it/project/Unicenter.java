package it.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import it.project.controller.*;
import it.project.database.ClockProvider;
import it.project.database.DatabasePopulator;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.UtenteNonTrovatoException;

public class Unicenter {
    private final List<Utente> utenti;
    private final ImmatricolazioneController immatricolazioneController;
    private final GestioneAppelliController gestioneAppelliController;
    private final GestoreMaterieController gestoreMaterie;
    private final GestioneCorsiLaureaController gestioneCorsiLaureaController;
    private final GestioneVotoController gestioneVotoController;
    private final InvioComunicazioniController invioComunicazioniController;
    private final PianoStudiController pianoStudiController;
    private final MaterialeDidatticoController materialeDidatticoController;
    private final MenuController menuController;
    private Utente currentUser = null;

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

        // 4. Inizializzazione Controller UC7 (Invio Comunicazioni)
        this.invioComunicazioniController = new InvioComunicazioniController(this, this.gestoreMaterie);

        // 5. Inizializzazione Controller UC9 (Compilazione Piano di Studi)
        this.pianoStudiController = new PianoStudiController(
                this.gestioneCorsiLaureaController,
                this.gestoreMaterie,
                this.gestioneAppelliController,
                this);

        // 6. Inizializzazione Controller UC6 & UC10 (Materiale Didattico)
        this.materialeDidatticoController = new MaterialeDidatticoController(this, this.gestoreMaterie);
    }

    private static class UnicenterHolder {
        private static final Unicenter INSTANCE = new Unicenter();
    }

    public static Unicenter getInstance() {
        return UnicenterHolder.INSTANCE;
    }

    public void avvia() {
        System.out.println("[UNICENTER] Avvio del sistema UniCenter...");
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

        // UC5 & UC9: Auto-popola il piano di studi con le materie obbligatorie del
        // corso
        if (corsoTrovato != null && corsoTrovato.isFinalizzato()) {
            PianoDiStudi pianoDiStudi = nuovoStudente.getPianoDiStudi();
            for (Materia materia : corsoTrovato.getMaterie()) {
                pianoDiStudi.aggiungiMateriaObbligatoria(materia.getCodiceMateria());
            }
            // Observer: registra lo studente per ricevere notifiche su approvazione/rifiuto
            // piano
            pianoDiStudi.aggiungiOsservatore(nuovoStudente);
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

        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            return Collections.emptyList();
        }

        Studente studente = (Studente) this.currentUser;
        PianoDiStudi pianoDiStudi = studente.getPianoDiStudi();
        if (pianoDiStudi == null) {
            return Collections.emptyList();
        }
        // UC9: Le materie obbligatorie sono sempre iscrivibili se presenti nel piano
        List<String> materieIscrivibili = new ArrayList<>(pianoDiStudi.getIdMaterieObbligatorie());
        // Le materie a scelta solo se il piano è approvato (o registrato)
        if (pianoDiStudi.isApprovato()) {
            materieIscrivibili.addAll(pianoDiStudi.getIdMaterieAScelta());
        }
        return gestioneAppelliController.trovaAppelliPrenotabiliByStudente(studente, materieIscrivibili);
    }

    public List<Appello> trovaAppelliPrenotatiDalloStudente() {
        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        Studente studente = (Studente) this.currentUser;
        return gestioneAppelliController.appelliPrenotatiByStudente(studente);

    }

    public List<Appello> trovaAppelliProfessore() {
        if (this.currentUser == null || !(this.currentUser instanceof Professore)) {
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
        if (appello != null && appello.getDataOra().isBefore(ClockProvider.nowLocalDateTime())) {
            throw new IllegalStateException(
                    "Impossibile annullare la prenotazione: l'esame è già in corso o si è già svolto.");
        }
        return gestioneAppelliController.disiscriviStudente((Studente) this.currentUser, codiceAppello);
    }

    public void popolaDataBase() {
        new DatabasePopulator(this).popolaDataBase();
    }

    public void addUtente(Utente utente) {
        this.utenti.add(utente);
    }

    public void setCurrentUser(Utente currentUser) {
        this.currentUser = currentUser;
    }

    public GestioneAppelliController getGestioneAppelliController() {
        return gestioneAppelliController;
    }

    public GestioneVotoController getGestioneVotoController() {
        return gestioneVotoController;
    }

    public GestioneCorsiLaureaController getGestioneCorsiLaureaController() {
        return gestioneCorsiLaureaController;
    }

    public InvioComunicazioniController getInvioComunicazioniController() {
        return invioComunicazioniController;
    }

    /**
     * UC7: Il Professore autenticato invia un avviso/comunicazione per una materia
     * di cui è responsabile.
     */
    public int inviaComunicazioneMateria(String codiceMateria, String titolo, String messaggio) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può inviare comunicazioni di corso.");
        }
        Professore professore = (Professore) currentUser;
        return invioComunicazioniController.inviaComunicazione(professore, codiceMateria, titolo, messaggio);
    }

    /**
     * UC7: Restituisce gli studenti destinatari per una data materia.
     */
    public List<Studente> getStudentiDestinatariComunicazione(String codiceMateria) {
        return invioComunicazioniController.getStudentiDestinatari(codiceMateria);
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

    public Studente trovaStudenteByMatricola(String matricola) {
        return trovaStudente(matricola)
                .orElseThrow(() -> new IllegalArgumentException("Nessun studente trovato con matricola: " + matricola));
    }

    public Optional<Studente> trovaStudenteByEmail(String email) {
        return utenti.stream()
                .filter(u -> u instanceof Studente)
                .map(u -> (Studente) u)
                .filter(s -> s.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Professore> trovaProfessore(String idProfessore) {
        return utenti.stream()
                .filter(u -> u instanceof Professore)
                .map(u -> (Professore) u)
                .filter(p -> p.getIdProfessore().equals(idProfessore))
                .findFirst();
    }

    public Optional<Professore> trovaProfessoreByEmail(String email) {
        return utenti.stream()
                .filter(u -> u instanceof Professore)
                .map(u -> (Professore) u)
                .filter(p -> p.getEmail().equalsIgnoreCase(email))
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
     * L'Amministratore elimina definitivamente un Corso di Laurea non finalizzato o
     * obsoleto.
     */
    public boolean eliminaCorsoDiLaurea(String codice) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può eliminare un corso di laurea.");
        }
        return gestioneCorsiLaureaController.eliminaCorsoDiLaurea(codice);
    }

    /**
     * Cerca un Corso di Laurea per ID (codice).
     */
    public CorsoDiLaurea trovaCorsoDiLaureaById(String id) {
        return gestioneCorsiLaureaController.trovaCorsoDiLaureaById(id);
    }

    // =========================================================================
    // UC3 - FACADE METHODS (Gestione Voto)
    // =========================================================================

    /**
     * Il Professore pubblica l'esito di un esame.
     * L'esito può essere pubblicato solo a partire dal giorno dell'appello.
     */
    public EsameSostenuto pubblicaEsitoEsame(String codiceAppello, String matricolaStudente,
            String codiceMateria, int votoNumerico,
            boolean lode, int giorniScadenza) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore può pubblicare un esito.");
        }

        // Vincolo temporale: il professore può registrare l'esito solo a partire dal
        // giorno dell'appello
        Appello appello = gestioneAppelliController.trovaAppelloByIdAppello(codiceAppello);
        if (appello == null) {
            throw new IllegalArgumentException("Appello non trovato: " + codiceAppello);
        }
        if (ClockProvider.nowLocalDate().isBefore(appello.getDataOra().toLocalDate())) {
            throw new IllegalStateException(
                    "Non è possibile pubblicare l'esito prima del giorno dell'appello ("
                            + appello.getDataOra().toLocalDate() + ").");
        }

        Professore professore = (Professore) currentUser;
        return gestioneVotoController.pubblicaEsito(
                codiceAppello, matricolaStudente, codiceMateria,
                professore.getIdProfessore(), votoNumerico, lode, giorniScadenza);
    }

    /**
     * Lo Studente accetta il voto.
     */
    public boolean accettaVoto(String idVerbale) {
        return gestioneVotoController.accettaVoto(idVerbale);
    }

    /**
     * Lo Studente rifiuta il voto.
     */
    public boolean rifiutaVoto(String idVerbale) {
        return gestioneVotoController.rifiutaVoto(idVerbale);
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
            throw new IllegalStateException("Nessuno studente autenticato per accedere al libretto.");
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
                    "Hai saldato con successo le tasse universitarie di " + String.format("%.2f EUR", importoPagato)
                            + ".",
                    ClockProvider.nowLocalDateTime()));
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

    // =========================================================================
    // UC9 - METODI FACADE PIANO DI STUDI
    // =========================================================================

    public PianoStudiController getPianoStudiController() {
        return pianoStudiController;
    }

    /**
     * Lo studente autenticato compila il proprio piano di studi selezionando
     * materie a scelta (almeno 12 CFU).
     */
    public boolean compilaPianoDiStudi(List<String> codiciMaterieAScelta) {
        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            throw new IllegalStateException("Solo uno studente autenticato può compilare il proprio piano di studi.");
        }
        Studente studente = (Studente) this.currentUser;
        return pianoStudiController.compilaPianoDiStudi(studente, codiciMaterieAScelta);
    }

    /**
     * L'amministratore approva un piano di studi in attesa.
     */
    public boolean approvaPianoDiStudi(String matricola) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può approvare un piano di studi.");
        }
        return pianoStudiController.approvaPianoDiStudi(matricola);
    }

    /**
     * L'amministratore rifiuta un piano di studi in attesa.
     */
    public boolean rifiutaPianoDiStudi(String matricola) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può rifiutare un piano di studi.");
        }
        return pianoStudiController.rifiutaPianoDiStudi(matricola);
    }

    /**
     * Restituisce la mappa dei piani in attesa di approvazione (matricola ->
     * PianoDiStudi).
     */
    public java.util.Map<String, PianoDiStudi> getPianiInAttesaApprovazione() {
        return pianoStudiController.getPianiInAttesa();
    }

    /**
     * L'amministratore aggiunge una materia pre-approvata a un corso di laurea.
     */
    public void aggiungiMateriaPreApprovata(String codiceCorso, Materia materia) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può gestire le materie pre-approvate.");
        }
        pianoStudiController.aggiungiMateriaPreApprovata(codiceCorso, materia);
    }

    /**
     * L'amministratore rimuove una materia pre-approvata da un corso di laurea.
     */
    public void rimuoviMateriaPreApprovata(String codiceCorso, Materia materia) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può gestire le materie pre-approvate.");
        }
        pianoStudiController.rimuoviMateriaPreApprovata(codiceCorso, materia);
    }

    /**
     * Restituisce le materie a scelta disponibili per lo studente attualmente
     * loggato
     * (tutte le materie non appartenenti al manifesto del suo corso).
     */
    public List<Materia> getMaterieASceltaDisponibili() {
        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        Studente studente = (Studente) this.currentUser;
        return pianoStudiController.getMaterieASceltaDisponibili(studente);
    }

    /**
     * Restituisce le materie pre-approvate per un corso di laurea.
     */
    public List<Materia> getMateriePreApprovateByCorso(String codiceCorso) {
        return pianoStudiController.getMateriePreApprovateByCorso(codiceCorso);
    }

    /**
     * Restituisce i codici delle materie a scelta già verbalizzate per lo studente
     * corrente.
     */
    public List<String> getMaterieASceltaVerbalizzate() {
        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        Studente studente = (Studente) this.currentUser;
        return pianoStudiController.getMaterieASceltaVerbalizzate(studente);
    }

    // =========================================================================
    // UC6 & UC10 - FACADE METHODS (Materiale Didattico)
    // =========================================================================

    public MaterialeDidatticoController getMaterialeDidatticoController() {
        return materialeDidatticoController;
    }

    /**
     * UC6: Il professore autenticato crea una sottocartella in una materia.
     */
    public it.project.materiale.Cartella creaCartellaMateriale(String codiceMateria, String idCartellaGenitore,
                                                               String nomeCartella, String descrizione) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può creare cartelle di materiale.");
        }
        return materialeDidatticoController.creaCartella((Professore) currentUser, codiceMateria, idCartellaGenitore, nomeCartella, descrizione);
    }

    /**
     * UC6: Il professore autenticato carica un materiale didattico.
     */
    public it.project.materiale.MaterialeDidattico caricaMaterialeDidattico(String codiceMateria, String idCartellaGenitore,
                                                                           String nomeFile, String descrizione,
                                                                           it.project.materiale.TipoMateriale tipo,
                                                                           byte[] contenuto) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può caricare materiale didattico.");
        }
        return materialeDidatticoController.caricaMateriale((Professore) currentUser, codiceMateria, idCartellaGenitore, nomeFile, descrizione, tipo, contenuto);
    }

    /**
     * UC6: Il professore autenticato elimina un proprio materiale o sottocartella.
     */
    public boolean eliminaMaterialeDidattico(String codiceMateria, String idElemento) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può eliminare materiale didattico.");
        }
        return materialeDidatticoController.eliminaElemento((Professore) currentUser, codiceMateria, idElemento);
    }

    /**
     * UC10: Restituisce l'albero Composite dei materiali per una materia.
     */
    public it.project.materiale.Cartella getAlberoMaterialeMateria(String codiceMateria) {
        return materialeDidatticoController.getAlberoMateria(codiceMateria);
    }

    /**
     * UC10: Consulta l'anteprima polimorfica di un elemento.
     */
    public it.project.materiale.AnteprimaRisultato consultaMaterialeDidattico(String idElemento) {
        return materialeDidatticoController.consultaMateriale(idElemento);
    }

    /**
     * UC10: Scarica il contenuto binario del materiale.
     */
    public MaterialeDidatticoController.DownloadResponse scaricaMaterialeDidattico(String idElemento) {
        return materialeDidatticoController.scaricaMateriale(idElemento);
    }

    /**
     * UC10: Aggiunge/rimuove un elemento dai preferiti dello studente autenticato.
     */
    public boolean togglePreferitoMateriale(String idElemento) {
        if (!(currentUser instanceof Studente)) {
            throw new IllegalStateException("Solo uno studente autenticato può gestire i propri preferiti.");
        }
        return materialeDidatticoController.togglePreferito((Studente) currentUser, idElemento);
    }

    /**
     * UC10: Restituisce i materiali e le cartelle preferite dello studente autenticato.
     */
    public List<it.project.materiale.ElementoDidattico> getPreferitiMaterialeStudente() {
        if (!(currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        return materialeDidatticoController.getPreferitiStudente((Studente) currentUser);
    }

}

