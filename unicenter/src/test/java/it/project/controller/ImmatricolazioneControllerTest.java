package it.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    void setUp() {
        // Recupero istanza Singleton
        unicenter = Unicenter.getInstance();
        
        // Popola il DB di UniCenter inserendo "Ingegneria Informatica" 
        // e i corsi necessari affinché la validazione del Controller vada a buon fine
        unicenter.popolaDataBase();
        
        immatricolazioneController = new ImmatricolazioneController(unicenter);
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
                .setCorsoDiLaurea("Ingegneria Informatica")
                .build();

        assertNotNull(studente, "Lo studente costruito non deve essere null");
        assertEquals("Mario", studente.getNome(), "Il nome dello studente deve corrispondere");
        assertEquals("Rossi", studente.getCognome(), "Il cognome dello studente deve corrispondere");
    }

    @Test
    @DisplayName("Calcolo tasse tramite CalcoloTasseStandardStrategy")
    void testCalcoloTasseStrategy() {
        ICalcoloTasseStrategy strategy = new CalcoloTasseStandardStrategy();
        
        double importoTasse = strategy.calcolaTasse(500.0, false);
        assertEquals(500.0, importoTasse, "L'importo delle tasse calcolato non deve essere negativo");
    }

    @Test
    @DisplayName("Creazione studente tramite ImmatricolazioneController (Senza Persistenza)")
    void testImmatricolazioneFlussoCompleto() {
        String email = "email.test@example.com";
        String cf = "VRDGSP0000A123X";

        // Creazione validata via Controller
        Studente studenteImmatricolato = immatricolazioneController.immatricolaStudente(
                "Giuseppe", "Verdi", email, "password", "Ingegneria Informatica", 500.0, cf
        );

        // Verify delle properties dell'oggetto creato
        assertNotNull(studenteImmatricolato, "Lo studente generato dal Controller non deve essere null");
        assertNotNull(studenteImmatricolato.getMatricola(), "Lo studente deve avere una matricola assegnata");
        assertEquals("Giuseppe", studenteImmatricolato.getNome(), "Il nome deve coincidere");
        assertEquals("Verdi", studenteImmatricolato.getCognome(), "Il cognome deve coincidere");
        
        // NOTA: Le asserzioni sull'aggiunta alla lista utenti di Unicenter sono omesse
        // poiché l'attuale design demanda la chiamata utenti.add() a Unicenter stesso e 
        // non al Controller.
    }

    @Test
    @DisplayName("Immatricolazione con codice corso non valido solleva eccezione")
    void testImmatricolazioneCorsoInesistente() {
        // Verifica l'eccezione esatta attesa come da implementazione del Controller
        assertThrows(IllegalArgumentException.class, () -> {
            immatricolazioneController.immatricolaStudente(
                "Anna", "Bianchi", "email.errata@example.com", "password", "ingegneria elettrica", 500.0, "BNNNCK0000A123Y"
            );
        }, "Il tentativo di immatricolazione ad un corso inesistente deve sollevare IllegalArgumentException");
    }
}