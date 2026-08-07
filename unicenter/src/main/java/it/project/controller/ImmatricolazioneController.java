package it.project.controller;

import java.time.LocalDate;
import java.time.Month;

import it.project.Studente;
import it.project.builder.StudenteBuilder;
import it.project.exceptions.DataNonValidaException;
import it.project.strategy.CalcoloTasseStandardStrategy;
import it.project.strategy.ICalcoloTasseStrategy;

public class ImmatricolazioneController {
    private ICalcoloTasseStrategy calcoloTasseStrategy;

    public ImmatricolazioneController() {
        this.calcoloTasseStrategy = new CalcoloTasseStandardStrategy();
    }

    public Studente immatricolaStudente(String nome, String cognome, String email, String password, String corso,
            double tassaBaseCorso, String codiceFisale) {
        Studente studente = new StudenteBuilder()
                .setNome(nome)
                .setCognome(cognome)
                .setEmail(email)
                .setCorsoDiLaurea(corso)
                .setPassword(password)
                .setCodiceFiscale(codiceFisale)
                .build();

        studente.calcolaImportoTasse(calcoloTasseStrategy, tassaBaseCorso, false);
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
    