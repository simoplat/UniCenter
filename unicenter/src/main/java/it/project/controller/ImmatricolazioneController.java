package it.project.controller;

import java.time.LocalDate;
import java.time.Month;

import it.project.Carriera;
import it.project.CorsoDiLaurea;
import it.project.Studente;
import it.project.Unicenter;
import it.project.builder.StudenteBuilder;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.exceptions.DataNonValidaException;
import it.project.strategy.CalcoloTasseStandardStrategy;
import it.project.strategy.ICalcoloTasseStrategy;

public class ImmatricolazioneController {
    private ICalcoloTasseStrategy calcoloTasseStrategy;
    private Unicenter unicenter;
    // ImmatricolazioneController.java
    public static final double TASSA_IMMATRICOLAZIONE = 150.0;
    public static final double TASSA_RINNOVO_BASE = 250.0;
    private double tasseImmatricolazione = TASSA_IMMATRICOLAZIONE;
    private double tassaRinnovoBase = TASSA_RINNOVO_BASE;

    public ImmatricolazioneController(Unicenter unicenter) {
        this.calcoloTasseStrategy = new CalcoloTasseStandardStrategy();
        this.unicenter = unicenter;
    }

    public Studente immatricolaStudente(String nome, String cognome, String email, String password, String corso,
            String codiceFisale) {

        CorsoDiLaurea corsoTrovato = unicenter.trovaCorsoDiLaureaByNome(corso);

        Studente studente = new StudenteBuilder()
                .setNome(nome)
                .setCognome(cognome)
                .setEmail(email)
                .setCorsoDiLaurea(corsoTrovato.getId())
                .setPassword(password)
                .setCodiceFiscale(codiceFisale)
                .build();
        studente.calcolaTasse(calcoloTasseStrategy, tasseImmatricolazione);
        return studente;
    }

    public boolean validaDataImmatricolazione() throws DataNonValidaException {
        Month mese = it.project.database.ClockProvider.nowLocalDate().getMonth();
        if (mese != Month.AUGUST && mese != Month.SEPTEMBER) {
            throw new DataNonValidaException(
                    "Impossibile immatricolarsi: la finestra di immatricolazione è aperta solo dal 1° agosto al 30 settembre.");
        }
        return true;
    }

    public boolean validaDataRinnovoIscrizione() throws DataNonValidaException {
        Month mese = it.project.database.ClockProvider.nowLocalDate().getMonth();
        if (mese != Month.SEPTEMBER && mese != Month.OCTOBER && mese != Month.NOVEMBER && mese != Month.DECEMBER) {
            throw new DataNonValidaException(
                    "Impossibile rinnovare l'iscrizione: la finestra di rinnovo è aperta solo dal 1° settembre al 31 dicembre.");
        }
        return true;
    }

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

    public boolean isFinestraRinnovoAperta() {
        try {
            return validaDataRinnovoIscrizione();
        } catch (DataNonValidaException e) {
            return false;
        }
    }

    public boolean isFinestraRinnovoAperta(Studente studente) {
        try {
            return validaDataRinnovoIscrizione(studente);
        } catch (DataNonValidaException e) {
            return false;
        }
    }

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

    public void setCalcoloTasseStrategy(ICalcoloTasseStrategy calcoloTasseStrategy) {
        this.calcoloTasseStrategy = calcoloTasseStrategy;
    }

    public ICalcoloTasseStrategy getCalcoloTasseStrategy() {
        return this.calcoloTasseStrategy;
    }
}
