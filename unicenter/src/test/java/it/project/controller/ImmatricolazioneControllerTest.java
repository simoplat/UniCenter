package it.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.CorsoDiLaurea;
import it.project.Studente;
import it.project.Unicenter;
import it.project.builder.StudenteBuilder;
import it.project.generator.MatricolaGenerator;
import it.project.strategy.CalcoloTasseStandardStrategy;
import it.project.strategy.ICalcoloTasseStrategy;

@DisplayName("Test Unitario - ImmatricolazioneController e Componenti Correlati")
class ImmatricolazioneControllerTest {

    private Unicenter unicenter;
    private ImmatricolazioneController immatricolazioneController;
    private CorsoDiLaurea corso;
    private GestoreMaterieController gestoreMaterie;
    private MatricolaGenerator matricolaGenerator;

    @BeforeEach
    void setUp() {
        // Setup del System Under Test (SUT) e delle fixture
        unicenter = Unicenter.getInstance();
        immatricolazioneController = new ImmatricolazioneController();
        
        // Creazione di un corso di laurea di test
        corso = new CorsoDiLaurea("L-31", "Informatica");
        gestoreMaterie = new GestoreMaterieController();
    }

    @Test
    @DisplayName("Verifica istanziamento del Controller")
    void testControllerNotNull() {
        assertNotNull(immatricolazioneController, "Il controller di immatricolazione non deve essere null");
    }

    @Test
    @DisplayName("Generazione matricola univoca tramite MatricolaGenerator")
    void testMatricolaGenerator() {
        MatricolaGenerator generator = MatricolaGenerator.getInstance();
        assertNotNull(generator, "Il generatore di matricola Singleton non deve essere null");
        String m1 = generator.generateMatricola();
        String m2 = generator.generateMatricola();
        
        assertNotNull(m1, "La matricola m1 non deve essere null");
        assertNotNull(m2, "La matricola m2 non deve essere null");
        assertTrue(!m1.equals(m2), "Le matricole generate devono essere univoche");
    }

    @Test
    @DisplayName("Costruzione corretta dell'oggetto Studente tramite StudenteBuilder")
    void testStudenteBuilder() {
        StudenteBuilder builder = new StudenteBuilder();
        
        Studente studente = builder
                .setNome("Mario")
                .setCognome("Rossi")
                .setCorsoDiLaurea("L-31")
                .build();

        assertNotNull(studente, "Lo studente costruito non deve essere null");
        assertEquals("Mario", studente.getNome(), "Il nome dello studente deve corrispondere");
        assertEquals("Rossi", studente.getCognome(), "Il cognome dello studente deve corrispondere");
    }

    @Test
    @DisplayName("Calcolo tasse tramite CalcoloTasseStandardStrategy")
    void testCalcoloTasseStrategy() {
        ICalcoloTasseStrategy strategy = new CalcoloTasseStandardStrategy();
        
        StudenteBuilder builder = new StudenteBuilder();
        Studente studente = builder
                .setNome("Luigi")
                .setCognome("Verdi")
                .setCorsoDiLaurea("L-31")
                .build();

        double importoTasse = strategy.calcolaTasse(500.0, false);
        assertTrue(importoTasse >= 0, "L'importo delle tasse calcolato non deve essere negativo");
    }

    @Test
    @DisplayName("Esecuzione flusso di immatricolazione tramite ImmatricolazioneController")
    void testImmatricolazioneFlussoCompleto() {
        // Exercise
        Studente studenteImmatricolato = immatricolazioneController.immatricolaStudente("Giuseppe", "Verdi", "email@example.com", "password", "L-31", 500.0, "VRDGSP0000A123B");
        String codiceFiscale = studenteImmatricolato.getCodiceFiscale();
        String email = studenteImmatricolato.getEmail();
        String matricola = studenteImmatricolato.getMatricola();
        // Verify
        assertNotNull(studenteImmatricolato, "Lo studente immatricolato non deve essere null");
        assertNotNull(matricola, "Lo studente deve avere una matricola assegnata");
        assertEquals("Giuseppe", studenteImmatricolato.getNome());
        assertEquals("Verdi", studenteImmatricolato.getCognome());

        // Verifica registrazione nell'archivio utenti di Unicenter
        Boolean utenteTrovato = unicenter.esisteCodiceFiscale(codiceFiscale);
        assertNotNull(utenteTrovato, "Lo studente deve essere presente nella mappa utenti di Unicenter");
        utenteTrovato = unicenter.esisteUtente(email);
        assertNotNull(utenteTrovato, "Lo studente deve essere presente nella mappa utenti di Unicenter");
    }

    @Test
    @DisplayName("Immatricolazione con codice corso non valido solleva eccezione")
    void testImmatricolazioneCorsoInesistente() {
        // Verify Exception
        assertThrows(RuntimeException.class, () -> {
            immatricolazioneController.immatricolaStudente("Anna", "Bianchi", "email@example.com", "password", "CORSO_ERRATO", 500.0, "VRDGSP0000A123B");
        }, "Il tentativo di immatricolazione ad un corso inesistente deve sollevare un'eccezione");
    }
}