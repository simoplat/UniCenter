package it.project.builder;

import it.project.Studente;
import it.project.generator.MatricolaGenerator;

public class StudenteBuilder {
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String corsoDiLaurea;
    private String codiceFiscale;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public StudenteBuilder setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }
        this.nome = nome.trim();
        return this;
    }

    public StudenteBuilder setCognome(String cognome) {
        if (cognome == null || cognome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il cognome non può essere vuoto.");
        }
        this.cognome = cognome.trim();
        return this;
    }

    public StudenteBuilder setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email è obbligatoria e non può essere vuota.");
        }
        String emailPulita = email.trim().toLowerCase();

        if (!emailPulita.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Formato email non valido: " + email);
        }

        this.email = emailPulita;
        return this;
    }

    public StudenteBuilder setPassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("La password deve contenere almeno 4 caratteri.");
        }
        this.password = password;
        return this;
    }

    public StudenteBuilder setCorsoDiLaurea(String corso) {
        if (corso == null || corso.trim().isEmpty()) {
            throw new IllegalArgumentException("Il corso di laurea è obbligatorio.");
        }
        this.corsoDiLaurea = corso.trim();
        return this;
    }

    public StudenteBuilder setCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice fiscale è obbligatorio.");
        }
        this.codiceFiscale = codiceFiscale;
        return this;
    }

    public Studente build() {
        String matricola = MatricolaGenerator.getInstance().generateMatricola();
        return new Studente(matricola, nome, cognome, email, password, codiceFiscale, corsoDiLaurea);
    }
}