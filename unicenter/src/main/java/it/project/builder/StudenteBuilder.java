package it.project.builder;

import it.project.Studente;
import it.project.generator.MatricolaGenerator;

/**
 * Builder per la creazione fluente e validata di oggetti {@link Studente}.
 * Esegue le validazioni sintattiche di nome, cognome, email, password, codice fiscale e corso.
 */
public class StudenteBuilder {
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String corsoDiLaurea;
    private String codiceFiscale;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Costruttore di default.
     */
    public StudenteBuilder() {
    }

    /**
     * Imposta e valida il nome dello studente.
     *
     * @param nome nome (solo lettere e spazi)
     * @return questa istanza del builder
     * @throws IllegalArgumentException se il nome è nullo, vuoto o contiene caratteri non alfabetici
     */
    public StudenteBuilder setNome(String nome) {
        if (nome == null || nome.trim().isEmpty() || !nome.matches("^[A-Za-z\\s]+$")) {
            throw new IllegalArgumentException("Il nome non può essere vuoto e deve contenere solo lettere.");
        }
        this.nome = nome.trim();
        return this;
    }

    /**
     * Imposta e valida il cognome dello studente.
     *
     * @param cognome cognome (solo lettere e spazi)
     * @return questa istanza del builder
     * @throws IllegalArgumentException se il cognome è nullo, vuoto o non valido
     */
    public StudenteBuilder setCognome(String cognome) {
        if (cognome == null || cognome.trim().isEmpty() || !cognome.matches("^[A-Za-z\\s]+$")) {
            throw new IllegalArgumentException("Il cognome non può essere vuoto e deve contenere solo lettere.");
        }
        this.cognome = cognome.trim();
        return this;
    }

    /**
     * Imposta e valida l'indirizzo email dello studente.
     *
     * @param email indirizzo email
     * @return questa istanza del builder
     * @throws IllegalArgumentException se l'email è nulla, vuota o ha formato non valido
     */
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

    /**
     * Imposta e valida la password.
     *
     * @param password password di accesso (almeno 4 caratteri)
     * @return questa istanza del builder
     * @throws IllegalArgumentException se la password è nulla o minore di 4 caratteri
     */
    public StudenteBuilder setPassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("La password deve contenere almeno 4 caratteri.");
        }
        this.password = password;
        return this;
    }

    /**
     * Imposta e valida il corso di laurea.
     *
     * @param corso denominazione o identificativo del corso
     * @return questa istanza del builder
     * @throws IllegalArgumentException se il corso è nullo o vuoto
     */
    public StudenteBuilder setCorsoDiLaurea(String corso) {
        if (corso == null || corso.trim().isEmpty()) {
            throw new IllegalArgumentException("Il corso di laurea è obbligatorio.");
        }
        this.corsoDiLaurea = corso.trim();
        return this;
    }

    /**
     * Imposta e valida il codice fiscale.
     *
     * @param codiceFiscale codice fiscale
     * @return questa istanza del builder
     * @throws IllegalArgumentException se il codice fiscale è nullo o vuoto
     */
    public StudenteBuilder setCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice fiscale è obbligatorio.");
        }
        this.codiceFiscale = codiceFiscale;
        return this;
    }

    /**
     * Genera una matricola progressiva e costruisce l'oggetto {@link Studente}.
     *
     * @return nuova istanza di Studente
     */
    public Studente build() {
        String matricola = MatricolaGenerator.getInstance().generateMatricola();
        return new Studente(matricola, nome, cognome, email, password, codiceFiscale, corsoDiLaurea);
    }
}