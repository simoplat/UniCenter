package it.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import it.project.controller.*;
import it.project.database.ClockProvider;
import it.project.database.DatabasePopulator;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.UtenteNonTrovatoException;

/**
 * Facade e Controller principale del sistema UniCenter (Singleton Pattern).
 * Coordina tutti i sotto-controller di dominio: immatricolazione e rinnovi,
 * gestione corsi di laurea e materie, gestione appelli, verbalizzazione voti e libretto,
 * invio comunicazioni, piani di studio e materiale didattico.
 */
public class Unicenter {
    private List<Utente> utenti;
    private ImmatricolazioneController immatricolazioneController;
    private GestioneAppelliController gestioneAppelliController;
    private GestoreMaterieController gestoreMaterie;
    private GestioneCorsiLaureaController gestioneCorsiLaureaController;
    private GestioneVotoController gestioneVotoController;
    private InvioComunicazioniController invioComunicazioniController;
    private PianoStudiController pianoStudiController;
    private MaterialeDidatticoController materialeDidatticoController;
    private MenuController menuController;
    private Utente currentUser = null;

    /**
     * Costruttore privato per garantire il Pattern Singleton.
     */
    private Unicenter() {
        this.utenti = new ArrayList<>();
    }

    /**
     * Inizializza i controller di dominio se non già inizializzati.
     */
    public void initControllers() {
        if (this.menuController == null) {
            this.menuController = new MenuController(this);
            this.immatricolazioneController = new ImmatricolazioneController(this);
            this.gestioneAppelliController = new GestioneAppelliController(this);
            this.gestoreMaterie = new GestoreMaterieController();
            this.gestioneCorsiLaureaController = new GestioneCorsiLaureaController();
            this.gestioneVotoController = new GestioneVotoController(this, this.gestoreMaterie);
            this.invioComunicazioniController = new InvioComunicazioniController(this, this.gestoreMaterie);
            this.pianoStudiController = new PianoStudiController(
                    this.gestioneCorsiLaureaController,
                    this.gestoreMaterie,
                    this.gestioneAppelliController,
                    this);
            this.materialeDidatticoController = new MaterialeDidatticoController(this, this.gestoreMaterie);
        }
    }

    private static class UnicenterHolder {
        private static final Unicenter INSTANCE = new Unicenter();
        static {
            INSTANCE.initControllers();
        }
    }

    /**
     * Restituisce l'istanza Singleton di Unicenter.
     *
     * @return istanza condivisa di Unicenter
     */
    public static Unicenter getInstance() {
        return UnicenterHolder.INSTANCE;
    }

    /**
     * Avvia il sistema UniCenter popolando il database in memoria e avviando la console.
     */
    public void avvia() {
        System.out.println("[UNICENTER] Avvio del sistema UniCenter...");
        popolaDataBase();
        menuController.avvia();
    }

    /**
     * Immatricola un nuovo studente al sistema UniCenter (UC1).
     *
     * @param nome          nome dello studente
     * @param cognome       cognome dello studente
     * @param email         indirizzo email
     * @param password      password di accesso
     * @param corso         denominazione del corso di laurea prescelto
     * @param codiceFiscale codice fiscale
     * @return l'istanza dello Studente creato e immatricolato
     * @throws IllegalArgumentException se email/codice fiscale sono duplicati o il corso non è valido/finalizzato
     */
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

    /**
     * Crea un nuovo appello d'esame (UC2).
     *
     * @param codiceMateria         codice della materia
     * @param dataOraStr            data e ora dell'esame
     * @param aula                  aula d'esame
     * @param postiDisponibili      posti massimi
     * @param vincoloLetteraCognome eventuale vincolo alfabetico
     * @param termineIscrizione     data di scadenza iscrizioni
     * @return true se creato con successo
     * @throws Exception in caso di errori di validazione
     */
    public boolean creaNuovoAppello(String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili,
            String vincoloLetteraCognome, LocalDate termineIscrizione) throws Exception {
        return gestioneAppelliController.creaNuovoAppello(codiceMateria, dataOraStr, aula, postiDisponibili,
                vincoloLetteraCognome, termineIscrizione);
    }

    /**
     * Restituisce la lista degli appelli prenotabili dallo studente autenticato.
     *
     * @return lista appelli prenotabili
     */
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

    /**
     * Restituisce la lista degli appelli già prenotati dallo studente autenticato.
     *
     * @return lista appelli prenotati
     */
    public List<Appello> trovaAppelliPrenotatiDalloStudente() {
        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        Studente studente = (Studente) this.currentUser;
        return gestioneAppelliController.appelliPrenotatiByStudente(studente);

    }

    /**
     * Restituisce gli appelli gestiti dal professore autenticato.
     *
     * @return lista appelli del professore
     */
    public List<Appello> trovaAppelliProfessore() {
        if (this.currentUser == null || !(this.currentUser instanceof Professore)) {
            return Collections.emptyList();
        }
        Professore professore = (Professore) currentUser;
        List<String> idMaterie = gestoreMaterie.trovaIdMaterieDiProfessore(professore.getIdProfessore());
        return gestioneAppelliController.trovaAppelliByIdMateria(idMaterie);
    }

    /**
     * Restituisce la lista degli studenti iscritti a un dato appello.
     *
     * @param codiceAppello codice appello
     * @return lista studenti iscritti
     */
    public List<Studente> trovaIscrittiByAppello(String codiceAppello) {

        return gestioneAppelliController.trovaIscrittiByIdAppello(codiceAppello);
    }

    /**
     * Iscrive lo studente autenticato all'appello specificato.
     *
     * @param codiceAppello codice dell'appello
     * @return true se l'iscrizione ha successo
     * @throws Exception in caso di fallimento della validazione o errore
     */
    public boolean iscriviStudenteAdAppello(String codiceAppello) throws Exception {
        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            throw new IllegalStateException("Solo uno studente autenticato può iscriversi a un appello.");
        }
        return gestioneAppelliController.iscriviStudente((Studente) this.currentUser, codiceAppello);
    }

    /**
     * Disiscrive lo studente autenticato dall'appello specificato.
     *
     * @param codiceAppello codice dell'appello
     * @return true se la disiscrizione ha successo
     * @throws Exception in caso di errore o esame già svolto
     */
    public boolean disiscriviStudenteDaAppello(String codiceAppello) throws Exception {
        if (this.currentUser == null || !(this.currentUser instanceof Studente)) {
            throw new IllegalStateException("Solo uno studente autenticato può disiscriversi da un appello.");
        }
        // Vincolo: la disiscrizione è bloccata se la data dell'esame è già passata
        Appello appello = gestioneAppelliController.trovaAppelloByIdAppello(codiceAppello);
        if (appello != null && appello.getDataOra().isBefore(ClockProvider.nowLocalDateTime())) {
            throw new IllegalStateException(
                    "Impossibile annullare la prenotazione: l'esame è già in corso o si è già svolto.");
        }
        return gestioneAppelliController.disiscriviStudente((Studente) this.currentUser, codiceAppello);
    }

    /**
     * Popola il database in memoria con i dati di simulazione e test.
     */
    public void popolaDataBase() {
        new DatabasePopulator(this).popolaDataBase();
    }

    /**
     * Aggiunge un utente al sistema.
     *
     * @param utente utente da aggiungere
     */
    public void addUtente(Utente utente) {
        this.utenti.add(utente);
    }

    /**
     * Imposta l'utente attualmente loggato nella sessione.
     *
     * @param currentUser utente corrente
     */
    public void setCurrentUser(Utente currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Restituisce il controller per la gestione degli appelli.
     *
     * @return GestioneAppelliController
     */
    public GestioneAppelliController getGestioneAppelliController() {
        return gestioneAppelliController;
    }

    /**
     * Restituisce il controller per la gestione dei voti.
     *
     * @return GestioneVotoController
     */
    public GestioneVotoController getGestioneVotoController() {
        return gestioneVotoController;
    }

    /**
     * Restituisce il controller per la gestione dei corsi di laurea.
     *
     * @return GestioneCorsiLaureaController
     */
    public GestioneCorsiLaureaController getGestioneCorsiLaureaController() {
        return gestioneCorsiLaureaController;
    }

    /**
     * Restituisce il controller per l'invio delle comunicazioni.
     *
     * @return InvioComunicazioniController
     */
    public InvioComunicazioniController getInvioComunicazioniController() {
        return invioComunicazioniController;
    }

    /**
     * UC7: Il Professore autenticato invia un avviso/comunicazione per una materia
     * di cui è responsabile.
     *
     * @param codiceMateria codice della materia
     * @param titolo        titolo dell'avviso
     * @param messaggio     testo del messaggio
     * @return numero di studenti raggiunti dalla comunicazione
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
     *
     * @param codiceMateria codice della materia
     * @return lista studenti destinatari
     */
    public List<Studente> getStudentiDestinatariComunicazione(String codiceMateria) {
        return invioComunicazioniController.getStudentiDestinatari(codiceMateria);
    }

    /**
     * Esegue il login dell'utente verificando email e password.
     *
     * @param email    email dell'utente
     * @param password password di accesso
     * @return l'istanza dell'Utente autenticato
     * @throws UtenteNonTrovatoException se le credenziali non sono valide
     */
    public Utente effettuaLogin(String email, String password) throws UtenteNonTrovatoException {
        for (Utente u : utenti) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                return u;
            }
        }
        throw new UtenteNonTrovatoException("Credenziali non valide: email o password errati.");
    }

    /**
     * Cerca uno studente per matricola restituendo un Optional.
     *
     * @param matricola matricola da cercare
     * @return Optional con lo studente se presente
     */
    public Optional<Studente> trovaStudente(String matricola) {
        return utenti.stream()
                .filter(u -> u instanceof Studente)
                .map(u -> (Studente) u)
                .filter(s -> s.getMatricola().equalsIgnoreCase(matricola))
                .findFirst();
    }

    /**
     * Cerca uno studente per matricola o solleva eccezione.
     *
     * @param matricola matricola da cercare
     * @return Studente trovato
     * @throws IllegalArgumentException se lo studente non esiste
     */
    public Studente trovaStudenteByMatricola(String matricola) {
        return trovaStudente(matricola)
                .orElseThrow(() -> new IllegalArgumentException("Nessun studente trovato con matricola: " + matricola));
    }

    /**
     * Cerca uno studente per indirizzo email.
     *
     * @param email indirizzo email
     * @return Optional con lo studente se presente
     */
    public Optional<Studente> trovaStudenteByEmail(String email) {
        return utenti.stream()
                .filter(u -> u instanceof Studente)
                .map(u -> (Studente) u)
                .filter(s -> s.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Cerca un docente per identificativo professore.
     *
     * @param idProfessore id del professore
     * @return Optional con il Professore se trovato
     */
    public Optional<Professore> trovaProfessore(String idProfessore) {
        return utenti.stream()
                .filter(u -> u instanceof Professore)
                .map(u -> (Professore) u)
                .filter(p -> p.getIdProfessore().equals(idProfessore))
                .findFirst();
    }

    /**
     * Cerca un docente per indirizzo email.
     *
     * @param email indirizzo email
     * @return Optional con il Professore se trovato
     */
    public Optional<Professore> trovaProfessoreByEmail(String email) {
        return utenti.stream()
                .filter(u -> u instanceof Professore)
                .map(u -> (Professore) u)
                .filter(p -> p.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Restituisce la lista di tutti gli studenti iscritti.
     *
     * @return lista studenti iscritti
     */
    public List<Studente> getStudentiIscritti() {
        List<Studente> studenti = new ArrayList<>();
        for (Utente u : utenti) {
            if (u instanceof Studente) {
                studenti.add((Studente) u);
            }
        }
        return studenti;
    }

    /**
     * Verifica se esiste già un utente registrato con la data email.
     *
     * @param email email da verificare
     * @return true se presente
     */
    public boolean esisteUtente(String email) {
        for (Utente u : utenti) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se esiste già un utente registrato con il dato codice fiscale.
     *
     * @param codiceFiscale codice fiscale da verificare
     * @return true se presente
     */
    public boolean esisteCodiceFiscale(String codiceFiscale) {
        for (Utente u : utenti) {
            if (u.getCodiceFiscale().equalsIgnoreCase(codiceFiscale)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica la correttezza delle credenziali e imposta l'utente loggato.
     *
     * @param email    email utente
     * @param password password utente
     * @return true se credenziali corrette, false altrimenti
     */
    public boolean passwordCorretta(String email, String password) {
        for (Utente u : utenti) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                this.currentUser = u; // Imposta l'utente corrente dopo il login
                return true;
            }
        }
        return false;

    }

    /**
     * Restituisce l'utente attualmente loggato nella sessione.
     *
     * @return utente corrente o null
     */
    public Utente getCurrentUser() {
        return currentUser;
    }

    /**
     * Restituisce le materie assegnate al professore attualmente loggato.
     *
     * @return lista materie del docente
     */
    public List<Materia> getMaterieDelProfessore() {
        if (this.currentUser == null || !(this.currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può accedere alle proprie materie.");
        }
        Professore professore = (Professore) getCurrentUser();
        return gestoreMaterie.trovaMaterieDiProfessore(professore.getIdProfessore());
    }

    /**
     * Verifica se il professore loggato è abilitato alla gestione di una determinata materia.
     *
     * @param codiceMateria codice materia
     * @return true se abilitato
     */
    public boolean isProfessoreAbilitatoAMateria(String codiceMateria) {
        if (this.currentUser == null || !(this.currentUser instanceof Professore)) {
            return false;
        }
        Professore professore = (Professore) getCurrentUser();
        return gestoreMaterie.isProfessoreAbilitatoAMateria(professore.getIdProfessore(), codiceMateria);
    }

    /**
     * Genera un nuovo codice univoco per un appello.
     *
     * @return codice appello generato
     */
    public String generaCodiceAppello() {
        return gestioneAppelliController.generaCodiceAppello();
    }

    /**
     * Restituisce le notifiche per lo studente attualmente loggato.
     *
     * @return lista notifiche dello studente
     */
    public List<Notifica> getNotifichePerStudente() {
        if (currentUser instanceof Studente) {
            Studente studente = (Studente) currentUser;
            return studente.getNotifiche();
        }
        return Collections.emptyList();
    }

    /**
     * Modifica i dettagli logistici di un appello d'esame.
     *
     * @param codiceAppello         codice dell'appello
     * @param dataOra               nuova data e ora
     * @param aula                  nuova aula
     * @param postiDisponibili      nuovo numero di posti
     * @param vincolo               nuovo vincolo cognome
     * @param dataTermineIscrizione nuovo termine iscrizioni
     * @return true se modificato con successo
     * @throws Exception in caso di errori di validazione
     */
    public boolean modificaAppello(String codiceAppello, LocalDateTime dataOra, String aula, int postiDisponibili,
            String vincolo, LocalDate dataTermineIscrizione) throws Exception {
        return gestioneAppelliController.modificaAppello(codiceAppello, dataOra, aula, postiDisponibili, vincolo,
                dataTermineIscrizione);
    }

    /**
     * Elimina un appello d'esame.
     *
     * @param codiceAppello codice appello
     * @return true se eliminato con successo
     */
    public boolean eliminaAppello(String codiceAppello) {
        return gestioneAppelliController.eliminaAppello(codiceAppello);
    }

    /**
     * Cerca un corso di laurea tramite la sua denominazione.
     *
     * @param nomeCorsoDiLaurea nome del corso
     * @return CorsoDiLaurea trovato o null
     */
    public CorsoDiLaurea trovaCorsoDiLaureaByNome(String nomeCorsoDiLaurea) {
        return gestioneCorsiLaureaController.trovaCorsoDiLaureaByNome(nomeCorsoDiLaurea);
    }

    /**
     * Valida se la data corrente rientra nella finestra temporale per l'immatricolazione.
     *
     * @return true se valida
     * @throws DataNonValidaException se fuori dalla finestra consentita
     */
    public boolean validaDataImmatricolazione() throws DataNonValidaException {
        return immatricolazioneController.validaDataImmatricolazione();
    }

    /**
     * Valida se la data corrente rientra nella finestra temporale per il rinnovo.
     *
     * @return true se valida
     * @throws DataNonValidaException se fuori dalla finestra consentita
     */
    public boolean validaDataRinnovoIscrizione() throws DataNonValidaException {
        return immatricolazioneController.validaDataRinnovoIscrizione();
    }

    /**
     * Verifica se la finestra temporale generale di rinnovo iscrizione è attualmente aperta.
     *
     * @return true se aperta
     */
    public boolean isRinnovoIscrizioneAperto() {
        return immatricolazioneController.isFinestraRinnovoAperta();
    }

    /**
     * Esegue il rinnovo dell'iscrizione per lo studente specificato (UC8).
     *
     * @param studente studente per cui rinnovare l'iscrizione
     * @return true se rinnovata con successo
     * @throws Exception in caso di vincoli violati
     */
    public boolean rinnovaIscrizioneStudente(Studente studente) throws Exception {
        return immatricolazioneController.rinnovaIscrizioneStudente(studente);
    }

    /**
     * Esegue il rinnovo dell'iscrizione per lo studente attualmente autenticato.
     *
     * @return true se rinnovata con successo
     * @throws Exception se non vi è uno studente loggato o in caso di errore
     */
    public boolean rinnovaIscrizioneStudenteCorrente() throws Exception {
        if (currentUser instanceof Studente) {
            return rinnovaIscrizioneStudente((Studente) currentUser);
        }
        throw new IllegalStateException("Nessuno studente autenticato per effettuare il rinnovo.");
    }

    /**
     * Restituisce una mappa riassuntiva sullo stato di idoneità al rinnovo per un dato studente.
     *
     * @param studente studente da analizzare
     * @return mappa con parametri di stato rinnovo
     */
    public Map<String, Object> getStatoRinnovoStudente(Studente studente) {
        return immatricolazioneController.getStatoRinnovoStudente(studente);
    }

    /**
     * Restituisce le informazioni sullo stato di rinnovo per lo studente attualmente autenticato.
     *
     * @return mappa dei dettagli di rinnovo
     */
    public Map<String, Object> getStatoRinnovoStudenteCorrente() {
        if (currentUser instanceof Studente) {
            return getStatoRinnovoStudente((Studente) currentUser);
        }
        return Collections.emptyMap();
    }

    /**
     * Restituisce la lista di tutti i corsi di laurea registrati.
     *
     * @return lista completa corsi di laurea
     */
    public List<CorsoDiLaurea> getCorsiDiLaurea() {
        return gestioneCorsiLaureaController.getTuttiCorsi();
    }

    /**
     * Restituisce solo i corsi attivi (non obsoleti) — usato dall'immatricolazione.
     *
     * @return lista corsi di laurea attivi
     */
    public List<CorsoDiLaurea> getCorsiDiLaureaAttivi() {
        return gestioneCorsiLaureaController.getCorsiAttivi();
    }

    // =========================================================================
    // UC4 - FACADE METHODS (Gestione Corsi di Laurea)
    // =========================================================================

    /**
     * L'Amministratore crea un nuovo Corso di Laurea.
     *
     * @param nome           denominazione del corso
     * @param tipologia      tipologia del corso
     * @param anniAccademici durata legale in anni
     * @return il nuovo CorsoDiLaurea
     */
    public CorsoDiLaurea creaCorsoDiLaurea(String nome, String tipologia, int anniAccademici) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può creare un corso di laurea.");
        }
        return gestioneCorsiLaureaController.creaCorsoDiLaurea(nome, tipologia, anniAccademici);
    }

    /**
     * L'Amministratore aggiorna un Corso di Laurea esistente.
     *
     * @param codice         codice del corso
     * @param nuovoNome      nuovo nome
     * @param nuovaTipologia nuova tipologia
     * @return true se aggiornato con successo
     */
    public boolean aggiornaCorsoDiLaurea(String codice, String nuovoNome, String nuovaTipologia) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può aggiornare un corso di laurea.");
        }
        return gestioneCorsiLaureaController.aggiornaCorsoDiLaurea(codice, nuovoNome, nuovaTipologia);
    }

    /**
     * L'Amministratore rende obsoleto un Corso di Laurea (soft-delete).
     *
     * @param codice codice del corso
     * @return true se reso obsoleto con successo
     */
    public boolean rendiObsoletoCorsoDiLaurea(String codice) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può rendere obsoleto un corso di laurea.");
        }
        return gestioneCorsiLaureaController.rendiObsoletoCorsoDiLaurea(codice);
    }

    /**
     * L'Amministratore elimina definitivamente un Corso di Laurea non finalizzato o obsoleto.
     *
     * @param codice codice del corso
     * @return true se eliminato con successo
     */
    public boolean eliminaCorsoDiLaurea(String codice) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può eliminare un corso di laurea.");
        }
        return gestioneCorsiLaureaController.eliminaCorsoDiLaurea(codice);
    }

    /**
     * Cerca un Corso di Laurea per ID (codice).
     *
     * @param id codice corso
     * @return CorsoDiLaurea trovato o null
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
     *
     * @param codiceAppello     codice appello
     * @param matricolaStudente matricola studente
     * @param codiceMateria     codice materia
     * @param votoNumerico      voto numerico (0-30)
     * @param lode              true se lode
     * @param giorniScadenza    giorni per accettazione/rifiuto
     * @return l'istanza di EsameSostenuto creata
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
     *
     * @param idVerbale identificativo del verbale
     * @return true se accettato con successo
     */
    public boolean accettaVoto(String idVerbale) {
        return gestioneVotoController.accettaVoto(idVerbale);
    }

    /**
     * Lo Studente rifiuta il voto.
     *
     * @param idVerbale identificativo del verbale
     * @return true se rifiutato con successo
     */
    public boolean rifiutaVoto(String idVerbale) {
        return gestioneVotoController.rifiutaVoto(idVerbale);
    }

    /**
     * Verifica le scadenze per il silenzio-rifiuto (Estensione A).
     *
     * @return numero di esami portati a scadenza/rifiuto
     */
    public int verificaScadenzeVoti() {
        return gestioneVotoController.verificaScadenze();
    }

    /**
     * Restituisce gli esiti pendenti dello studente corrente.
     *
     * @return lista esiti pendenti
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
     *
     * @return lista tutti gli esiti dello studente
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
     *
     * @return lista esiti pubblicati dal docente
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
     *
     * @return libretto dello studente
     */
    public Libretto getLibrettoStudente() {
        if (!(currentUser instanceof Studente)) {
            throw new IllegalStateException("Nessuno studente autenticato per accedere al libretto.");
        }
        return ((Studente) currentUser).getLibretto();
    }

    /**
     * Restituisce l'importo delle tasse dello studente corrente.
     *
     * @return importo tasse
     */
    public double getTasseStudente() {
        if (currentUser instanceof Studente) {
            return ((Studente) currentUser).getTasse();
        }
        return 0.0;
    }

    /**
     * Verifica se le tasse dello studente corrente risultano saldate.
     *
     * @return true se pagate
     */
    public boolean isTassePagateStudente() {
        if (currentUser instanceof Studente) {
            return ((Studente) currentUser).isTassePagate();
        }
        return false;
    }

    /**
     * Simula il pagamento delle tasse per lo studente corrente.
     *
     * @return true se pagamento registrato
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

    /**
     * Restituisce il controller di gestione materie.
     *
     * @return GestoreMaterieController
     */
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
     *
     * @param nome nome della materia
     * @param cfu  numero di crediti
     * @return Materia creata
     */
    public Materia creaMateria(String nome, int cfu) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può creare una materia.");
        }
        return gestoreMaterie.creaMateria(nome, cfu);
    }

    /**
     * Restituisce tutte le materie create nel sistema.
     *
     * @return lista di tutte le materie
     */
    public List<Materia> getTutteLeMaterie() {
        return gestoreMaterie.getTutteLeMaterie();
    }

    /**
     * Restituisce i corsi non ancora finalizzati (senza materie associate).
     *
     * @return lista corsi non finalizzati
     */
    public List<CorsoDiLaurea> getCorsiNonFinalizzati() {
        return gestioneCorsiLaureaController.getCorsiNonFinalizzati();
    }

    /**
     * L'Amministratore associa una materia a un anno di un corso non finalizzato.
     *
     * @param codiceCorso codice corso
     * @param anno        anno di corso (1-based)
     * @param materia     materia da associare
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
     *
     * @param codiceCorso codice del corso da finalizzare
     */
    public void finalizzaCorso(String codiceCorso) {
        if (!(currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può finalizzare un corso.");
        }
        gestioneCorsiLaureaController.finalizzaCorso(codiceCorso);
    }

    /**
     * Restituisce tutti i professori registrati nel sistema.
     *
     * @return lista di tutti i professori
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
     *
     * @param codiceMateria codice della materia
     * @return lista professori non associati
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
     *
     * @param idProfessore  id del docente
     * @param codiceMateria codice della materia
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

    /**
     * Restituisce il controller per la gestione dei piani di studi.
     *
     * @return PianoStudiController
     */
    public PianoStudiController getPianoStudiController() {
        return pianoStudiController;
    }

    /**
     * Lo studente autenticato compila il proprio piano di studi selezionando
     * materie a scelta (almeno 12 CFU).
     *
     * @param codiciMaterieAScelta lista dei codici materia a scelta
     * @return true se compilazione riuscita
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
     *
     * @param matricola matricola dello studente
     * @return true se approvato con successo
     */
    public boolean approvaPianoDiStudi(String matricola) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può approvare un piano di studi.");
        }
        return pianoStudiController.approvaPianoDiStudi(matricola);
    }

    /**
     * L'amministratore rifiuta un piano di studi in attesa.
     *
     * @param matricola matricola dello studente
     * @return true se rifiutato con successo
     */
    public boolean rifiutaPianoDiStudi(String matricola) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può rifiutare un piano di studi.");
        }
        return pianoStudiController.rifiutaPianoDiStudi(matricola);
    }

    /**
     * Restituisce la mappa dei piani in attesa di approvazione (matricola -&gt; PianoDiStudi).
     *
     * @return mappa matricola -&gt; PianoDiStudi in attesa
     */
    public java.util.Map<String, PianoDiStudi> getPianiInAttesaApprovazione() {
        return pianoStudiController.getPianiInAttesa();
    }

    /**
     * L'amministratore aggiunge una materia pre-approvata a un corso di laurea.
     *
     * @param codiceCorso codice del corso
     * @param materia     materia pre-approvata da aggiungere
     */
    public void aggiungiMateriaPreApprovata(String codiceCorso, Materia materia) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può gestire le materie pre-approvate.");
        }
        pianoStudiController.aggiungiMateriaPreApprovata(codiceCorso, materia);
    }

    /**
     * L'amministratore rimuove una materia pre-approvata da un corso di laurea.
     *
     * @param codiceCorso codice del corso
     * @param materia     materia pre-approvata da rimuovere
     */
    public void rimuoviMateriaPreApprovata(String codiceCorso, Materia materia) {
        if (this.currentUser == null || !(this.currentUser instanceof Amministratore)) {
            throw new IllegalStateException("Solo un amministratore può gestire le materie pre-approvate.");
        }
        pianoStudiController.rimuoviMateriaPreApprovata(codiceCorso, materia);
    }

    /**
     * Restituisce le materie a scelta disponibili per lo studente attualmente loggato
     * (tutte le materie non appartenenti al manifesto del suo corso).
     *
     * @return lista materie a scelta disponibili
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
     *
     * @param codiceCorso codice del corso
     * @return lista materie pre-approvate
     */
    public List<Materia> getMateriePreApprovateByCorso(String codiceCorso) {
        return pianoStudiController.getMateriePreApprovateByCorso(codiceCorso);
    }

    /**
     * Restituisce i codici delle materie a scelta già verbalizzate per lo studente corrente.
     *
     * @return lista codici materie verbalizzate
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

    /**
     * Restituisce il controller per il materiale didattico.
     *
     * @return MaterialeDidatticoController
     */
    public MaterialeDidatticoController getMaterialeDidatticoController() {
        return materialeDidatticoController;
    }

    /**
     * UC6: Il professore autenticato crea una sottocartella in una materia.
     *
     * @param codiceMateria      codice materia
     * @param idCartellaGenitore id cartella superiore o null per radice
     * @param nomeCartella       nome cartella
     * @param descrizione        descrizione cartella
     * @return la nuova Cartella creata
     */
    public it.project.materiale.Cartella creaCartellaMateriale(String codiceMateria, String idCartellaGenitore,
            String nomeCartella, String descrizione) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può creare cartelle di materiale.");
        }
        return materialeDidatticoController.creaCartella((Professore) currentUser, codiceMateria, idCartellaGenitore,
                nomeCartella, descrizione);
    }

    /**
     * UC6: Il professore autenticato carica un materiale didattico.
     *
     * @param codiceMateria      codice materia
     * @param idCartellaGenitore id cartella genitore
     * @param nomeFile           nome file
     * @param descrizione        descrizione
     * @param tipo               tipo materiale
     * @param contenuto          byte del file
     * @return il MaterialeDidattico creato
     */
    public it.project.materiale.MaterialeDidattico caricaMaterialeDidattico(String codiceMateria,
            String idCartellaGenitore,
            String nomeFile, String descrizione,
            it.project.materiale.TipoMateriale tipo,
            byte[] contenuto) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può caricare materiale didattico.");
        }
        return materialeDidatticoController.caricaMateriale((Professore) currentUser, codiceMateria, idCartellaGenitore,
                nomeFile, descrizione, tipo, contenuto);
    }

    /**
     * UC6: Il professore autenticato elimina un proprio materiale o sottocartella.
     *
     * @param codiceMateria codice materia
     * @param idElemento    id dell'elemento da eliminare
     * @return true se eliminato con successo
     */
    public boolean eliminaMaterialeDidattico(String codiceMateria, String idElemento) {
        if (!(currentUser instanceof Professore)) {
            throw new IllegalStateException("Solo un professore autenticato può eliminare materiale didattico.");
        }
        return materialeDidatticoController.eliminaElemento((Professore) currentUser, codiceMateria, idElemento);
    }

    /**
     * UC10: Restituisce l'albero Composite dei materiali per una materia.
     *
     * @param codiceMateria codice materia
     * @return Cartella radice della materia
     */
    public it.project.materiale.Cartella getAlberoMaterialeMateria(String codiceMateria) {
        return materialeDidatticoController.getAlberoMateria(codiceMateria);
    }

    /**
     * UC10: Consulta l'anteprima polimorfica di un elemento.
     *
     * @param idElemento id elemento
     * @return AnteprimaRisultato
     */
    public it.project.materiale.AnteprimaRisultato consultaMaterialeDidattico(String idElemento) {
        return materialeDidatticoController.consultaMateriale(idElemento);
    }

    /**
     * UC10: Scarica il contenuto binario del materiale.
     *
     * @param idElemento id elemento
     * @return DownloadResponse con nome file e byte
     */
    public MaterialeDidatticoController.DownloadResponse scaricaMaterialeDidattico(String idElemento) {
        return materialeDidatticoController.scaricaMateriale(idElemento);
    }

    /**
     * UC10: Aggiunge/rimuove un elemento dai preferiti dello studente autenticato.
     *
     * @param idElemento id elemento
     * @return true se aggiunto ai preferiti, false se rimosso
     */
    public boolean togglePreferitoMateriale(String idElemento) {
        if (!(currentUser instanceof Studente)) {
            throw new IllegalStateException("Solo uno studente autenticato può gestire i propri preferiti.");
        }
        return materialeDidatticoController.togglePreferito((Studente) currentUser, idElemento);
    }

    /**
     * UC10: Restituisce i materiali e le cartelle preferite dello studente
     * autenticato.
     *
     * @return lista degli elementi preferiti dello studente
     */
    public List<it.project.materiale.ElementoDidattico> getPreferitiMaterialeStudente() {
        if (!(currentUser instanceof Studente)) {
            return Collections.emptyList();
        }
        return materialeDidatticoController.getPreferitiStudente((Studente) currentUser);
    }

}
