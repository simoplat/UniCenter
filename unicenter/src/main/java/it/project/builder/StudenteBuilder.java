package it.project.builder;

import it.project.Studente;
import it.project.generator.MatricolaGenerator;

public class StudenteBuilder {
    private String nome;
    private String cognome;
    private String email;
    private String corsoDiLaurea;

    public StudenteBuilder setNome(String nome) {
        this.nome = nome;
        return this;
    }

    public StudenteBuilder setCognome(String cognome) {
        this.cognome = cognome;
        return this;
    }

    public StudenteBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public StudenteBuilder setCorsoDiLaurea(String corsoDiLaurea) {
        this.corsoDiLaurea = corsoDiLaurea;
        return this;
    }

    public Studente build() {
        if (nome == null || cognome == null || email == null) {
            throw new IllegalArgumentException("Dati studente incompleti per l'immatricolazione.");
        }
        String matricola = MatricolaGenerator.getInstance().generateMatricola();
        return new Studente(matricola, nome, cognome, email, corsoDiLaurea);
    }
}