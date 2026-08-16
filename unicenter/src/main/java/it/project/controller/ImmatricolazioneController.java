package it.project.controller;

import java.time.LocalDate;
import java.time.Month;

import it.project.Carriera;
import it.project.CorsoDiLaurea;
import it.project.Studente;
import it.project.Unicenter;
import it.project.builder.StudenteBuilder;
import it.project.exceptions.DataNonValidaException;
import it.project.strategy.CalcoloTasseStandardStrategy;
import it.project.strategy.ICalcoloTasseStrategy;

public class ImmatricolazioneController {
    private ICalcoloTasseStrategy calcoloTasseStrategy;
    private Unicenter unicenter;
    // ImmatricolazioneController.java
    public static final double TASSA_IMMATRICOLAZIONE = 150.0;
    private double tasseImmatricolazione = TASSA_IMMATRICOLAZIONE;

    public ImmatricolazioneController(Unicenter unicenter) {
        this.calcoloTasseStrategy = new CalcoloTasseStandardStrategy();
        this.unicenter = unicenter;
    }

    public Studente immatricolaStudente(String nome, String cognome, String email, String password, String corso,
            String codiceFisale) {

        CorsoDiLaurea corsoTrovato = unicenter.trovaCorsoDiLaureaByNome(corso);
        if (corsoTrovato == null) {
            throw new IllegalArgumentException("Impossibile immatricolarsi: corso non esistente (" + corso + ")");
        }

        Studente studente = new StudenteBuilder()
                .setNome(nome)
                .setCognome(cognome)
                .setEmail(email)
                .setCorsoDiLaurea(corso)
                .setPassword(password)
                .setCodiceFiscale(codiceFisale)
                .build();
        studente.calcolaTasse(calcoloTasseStrategy, tasseImmatricolazione);
        return studente;
    }

    public boolean validaDataImmatricolazione() throws DataNonValidaException {
        Month mese = LocalDate.now().getMonth();
        if (mese != Month.AUGUST && mese != Month.SEPTEMBER) {
            throw new DataNonValidaException(
                    "Impossibile immatricolarsi: la finestra di immatricolazione è aperta solo dal 1° agosto al 30 settembre.");
        }
        return true;
    }
}
