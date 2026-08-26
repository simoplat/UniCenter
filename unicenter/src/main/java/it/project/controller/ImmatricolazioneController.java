package it.project.controller;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.project.Carriera;
import it.project.CorsoDiLaurea;
import it.project.Studente;
import it.project.Unicenter;
import it.project.builder.StudenteBuilder;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.exceptions.DataNonValidaException;
import it.project.strategy.CalcoloTasseStandardStrategy;
import it.project.strategy.ICalcoloTasseStrategy;

/**
 * Controller per la gestione dell'immatricolazione (UC1) e del rinnovo delle iscrizioni annuali (UC8).
 * Gestisce i controlli sulle finestre temporali, il calcolo delle tasse universitarie
 * mediante Strategy Pattern e l'aggiornamento dello stato accademico dello studente.
 */
public class ImmatricolazioneController {
    private ICalcoloTasseStrategy calcoloTasseStrategy;
    private Unicenter unicenter;
    /** Tassa fissa iniziale di immatricolazione. */
    public static final double TASSA_IMMATRICOLAZIONE = 150.0;
    /** Tassa base per il rinnovo dell'iscrizione ad anni successivi. */
    public static final double TASSA_RINNOVO_BASE = 250.0;
    private double tasseImmatricolazione = TASSA_IMMATRICOLAZIONE;
    private double tassaRinnovoBase = TASSA_RINNOVO_BASE;

    /**
     * Costruttore del controller di immatricolazione.
     *
     * @param unicenter riferimento al sistema centrale UniCenter
     */
    public ImmatricolazioneController(Unicenter unicenter) {
        this.calcoloTasseStrategy = new CalcoloTasseStandardStrategy();
        this.unicenter = unicenter;
    }

    /**
     * Esegue l'immatricolazione di un nuovo studente (UC1) costruendo l'oggetto tramite {@link StudenteBuilder}.
     *
     * @param nome         nome dello studente
     * @param cognome      cognome dello studente
     * @param email        email istituzionale
     * @param password     password di accesso
     * @param corso        denominazione del corso di laurea
     * @param codiceFisale codice fiscale
     * @return nuova istanza di Studente immatricolato
     */
    public Studente immatricolaStudente(String nome, String cognome, String email, String password, String corso,
            String codiceFiscale) {

        CorsoDiLaurea corsoTrovato = unicenter.trovaCorsoDiLaureaByNome(corso);

        Studente studente = new StudenteBuilder()
                .setNome(nome)
                .setCognome(cognome)
                .setEmail(email)
                .setCorsoDiLaurea(corsoTrovato.getId())
                .setPassword(password)
                .setCodiceFiscale(codiceFiscale)
                .build();
        studente.calcolaTasse(calcoloTasseStrategy, tasseImmatricolazione);
        return studente;
    }

    /**
     * Valida se la data odierna rientra nella finestra consentita per l'immatricolazione (1 agosto - 30 settembre).
     *
     * @return true se valida
     * @throws DataNonValidaException se fuori dalla finestra
     */
    public boolean validaDataImmatricolazione() throws DataNonValidaException {
        Month mese = it.project.database.ClockProvider.nowLocalDate().getMonth();
        if (mese != Month.AUGUST && mese != Month.SEPTEMBER) {
            throw new DataNonValidaException(
                    "Impossibile immatricolarsi: la finestra di immatricolazione è aperta solo dal 1° agosto al 30 settembre.");
        }
        return true;
    }

    /**
     * Valida se la data odierna rientra nella finestra temporale generale di rinnovo (1 settembre - 31 dicembre).
     *
     * @return true se valida
     * @throws DataNonValidaException se fuori dalla finestra
     */
    public boolean validaDataRinnovoIscrizione() throws DataNonValidaException {
        Month mese = it.project.database.ClockProvider.nowLocalDate().getMonth();
        if (mese != Month.SEPTEMBER && mese != Month.OCTOBER && mese != Month.NOVEMBER && mese != Month.DECEMBER) {
            throw new DataNonValidaException(
                    "Impossibile rinnovare l'iscrizione: la finestra di rinnovo è aperta solo dal 1° settembre al 31 dicembre.");
        }
        return true;
    }

    /**
     * Valida sia la finestra temporale generale che il vincolo sull'anno di immatricolazione dello studente.
     *
     * @param studente studente richiedente il rinnovo
     * @return true se valida
     * @throws DataNonValidaException se fuori dalla finestra o nello stesso anno di immatricolazione
     */
    public boolean validaDataRinnovoIscrizione(Studente studente) throws DataNonValidaException {
        validaDataRinnovoIscrizione();
        if (studente != null) {
            int annoCorrente = it.project.database.ClockProvider.nowLocalDate().getYear();
            int annoImmatricolazione = studente.getAnnoImmatricolazione();
            if (annoCorrente <= annoImmatricolazione) {
                throw new DataNonValidaException(
                        "Non è possibile rinnovare l'iscrizione nello stesso anno solare di immatricolazione ("
                                + annoImmatricolazione + "). La prima finestra di rinnovo valida sarà attiva a partire dall'anno successivo ("
                                + (annoImmatricolazione + 1) + ").");
            }
        }
        return true;
    }

    /**
     * Verifica booleana sulla disponibilità della finestra generale di rinnovo.
     *
     * @return true se aperta, false altrimenti
     */
    public boolean isFinestraRinnovoAperta() {
        try {
            return validaDataRinnovoIscrizione();
        } catch (DataNonValidaException e) {
            return false;
        }
    }

    /**
     * Verifica booleana sulla disponibilità della finestra di rinnovo per un determinato studente.
     *
     * @param studente lo studente
     * @return true se idoneo per data, false altrimenti
     */
    public boolean isFinestraRinnovoAperta(Studente studente) {
        try {
            return validaDataRinnovoIscrizione(studente);
        } catch (DataNonValidaException e) {
            return false;
        }
    }

    /**
     * Elabora il rinnovo di iscrizione di uno studente applicando tutti i vincoli di business (UC8).
     *
     * @param studente studente da rinnovare
     * @return true se rinnovato con successo
     * @throws Exception in caso di violazione dei requisiti di rinnovo
     */
    public boolean rinnovaIscrizioneStudente(Studente studente) throws Exception {
        if (studente == null) {
            throw new IllegalArgumentException("Studente non valido.");
        }

        // 1. Controllo finestra temporale e verifica anno di immatricolazione
        validaDataRinnovoIscrizione(studente);

        // 2. Controllo tasse pregresse
        if (!studente.isTassePagate()) {
            throw new IllegalStateException(
                    "Impossibile rinnovare l'iscrizione: ci sono tasse universitarie pendenti relative all'anno precedente da saldare.");
        }

        // 3. Controllo doppi rinnovi nello stesso ciclo
        if (studente.isRinnovoEffettuatoPerAnnoCorrente()) {
            throw new IllegalStateException(
                    "Rinnovo dell'iscrizione già effettuato per l'anno accademico in corso.");
        }

        // 4. Recupero durata corso di laurea
        if (unicenter == null || unicenter.getGestioneCorsiLaureaController() == null) {
            throw new IllegalStateException("Controller per la gestione dei corsi di laurea non disponibile.");
        }
        CorsoDiLaurea corso = unicenter.getGestioneCorsiLaureaController()
                .trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());
        if (corso == null) {
            throw new CorsoDiLaureaNonTrovatoException(
                    "Corso di laurea non trovato per lo studente: " + studente.getIdCorsoDiLaurea());
        }
        int anniDurata = corso.getAnniAccademici();

        // 5. Esecuzione rinnovo e notifica
        studente.rinnovaIscrizione(anniDurata, calcoloTasseStrategy, tassaRinnovoBase);
        return true;
    }

    /**
     * Imposta la strategia di calcolo tasse (Strategy Pattern).
     *
     * @param calcoloTasseStrategy nuova strategia di calcolo
     */
    public void setCalcoloTasseStrategy(ICalcoloTasseStrategy calcoloTasseStrategy) {
        this.calcoloTasseStrategy = calcoloTasseStrategy;
    }

    /**
     * Restituisce la strategia corrente di calcolo tasse.
     *
     * @return ICalcoloTasseStrategy corrente
     */
    public ICalcoloTasseStrategy getCalcoloTasseStrategy() {
        return this.calcoloTasseStrategy;
    }

    /**
     * Restituisce una mappa riassuntiva sullo stato di idoneità al rinnovo per un dato studente.
     *
     * @param studente studente da analizzare
     * @return mappa con parametri di stato rinnovo
     */
    public Map<String, Object> getStatoRinnovoStudente(Studente studente) {
        if (studente == null) {
            return Collections.emptyMap();
        }
        boolean finestraAperta = isFinestraRinnovoAperta(studente);
        boolean finestraGeneraleAperta = isFinestraRinnovoAperta();
        boolean tassePregressePagate = studente.isTassePagate();
        boolean giaRinnovato = studente.isRinnovoEffettuatoPerAnnoCorrente();
        int annoAttuale = studente.getAnnoCorrente();
        int prossimoAnno = annoAttuale + 1;
        int annoImmatricolazione = studente.getAnnoImmatricolazione();

        if (unicenter == null || unicenter.getGestioneCorsiLaureaController() == null) {
            throw new IllegalStateException("Controller per la gestione dei corsi di laurea non disponibile.");
        }
        CorsoDiLaurea corso = unicenter.getGestioneCorsiLaureaController()
                .trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());
        if (corso == null) {
            throw new CorsoDiLaureaNonTrovatoException(
                    "Corso di laurea non trovato per lo studente: " + studente.getIdCorsoDiLaurea());
        }
        int durata = corso.getAnniAccademici();
        String nomeCorso = corso.getNome();

        boolean saraFuoriCorso = prossimoAnno > durata;
        double importoStimato = calcoloTasseStrategy.calcolaTasse(
                TASSA_RINNOVO_BASE, saraFuoriCorso);

        boolean idoneo = finestraAperta && tassePregressePagate && !giaRinnovato;

        String motivoBlocco = null;
        if (!finestraGeneraleAperta) {
            motivoBlocco = "La finestra temporale per il rinnovo è chiusa (aperta dal 1° settembre al 31 dicembre).";
        } else if (!finestraAperta) {
            motivoBlocco = "Non è possibile rinnovare l'iscrizione nello stesso anno solare di immatricolazione ("
                    + annoImmatricolazione + "). La prima finestra di rinnovo valida sarà attiva a partire dall'anno "
                    + (annoImmatricolazione + 1) + ".";
        } else if (!tassePregressePagate) {
            motivoBlocco = "È necessario prima saldare le tasse universitarie pendenti relative all'anno precedente.";
        } else if (giaRinnovato) {
            motivoBlocco = "Rinnovo già effettuato per l'anno accademico in corso.";
        }

        Map<String, Object> stato = new HashMap<>();
        stato.put("finestraAperta", finestraAperta);
        stato.put("finestraGeneraleAperta", finestraGeneraleAperta);
        stato.put("tassePregressePagate", tassePregressePagate);
        stato.put("giaRinnovato", giaRinnovato);
        stato.put("annoAttuale", annoAttuale);
        stato.put("prossimoAnno", prossimoAnno);
        stato.put("annoImmatricolazione", annoImmatricolazione);
        stato.put("isFuoriCorsoAttuale", studente.isFuoriCorso());
        stato.put("saraFuoriCorso", saraFuoriCorso);
        stato.put("durataCorso", durata);
        stato.put("nomeCorso", nomeCorso);
        stato.put("importoStimato", importoStimato);
        stato.put("idoneo", idoneo);
        stato.put("motivoBlocco", motivoBlocco);
        return stato;
    }
}
