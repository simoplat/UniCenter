package it.project.controller;

import it.project.Studente;
import it.project.builder.StudenteBuilder;
import it.project.strategy.CalcoloTasseStandardStrategy;
import it.project.strategy.ICalcoloTasseStrategy;

public class ImmatricolazioneController {
    private ICalcoloTasseStrategy calcoloTasseStrategy;

    public ImmatricolazioneController() {
        this.calcoloTasseStrategy = new CalcoloTasseStandardStrategy();
    }

    public Studente immatricolaStudente(String nome, String cognome, String email, String password, String corso, double tassaBaseCorso, String codiceFisale) {
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
}