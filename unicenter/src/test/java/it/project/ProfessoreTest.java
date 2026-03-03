package it.project;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class ProfessoreTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private Professore professore;

    @BeforeEach
    public void setUp() {
        // Redirigiamo l'output del sistema per poterlo leggere nei test
        System.setOut(new PrintStream(outContent));
        professore = new Professore(1,"mario","rossi","mrossi@email.com", "1234", "MRRS1023");
    }

    @AfterEach
    public void restoreStreams() {
        // Ripristiniamo l'output originale dopo ogni test
        System.setOut(originalOut);
    }

    @Test
    public void testMenuAppelli_VisualizzaEdEsci() {
        // Simuliamo l'utente che preme '1' (Visualizza) e poi '0' (Torna indietro)
        String fakedInput = "1\n0\n";
        Scanner sc = new Scanner(fakedInput);

        professore.menuAppelli(sc);

        String output = outContent.toString();
        
        // Verifichiamo che i messaggi attesi siano presenti nell'output
        assertTrue(output.contains("Visualizzazione degli appelli in corso..."));
        assertTrue(output.contains("Uscita dalla bacheca."));
    }

    @Test
    public void testMenuAppelli_InputNonValido() {
        // Simuliamo una stringa non numerica "abc" e poi '0' per uscire
        String fakedInput = "abc\n0\n";
        Scanner sc = new Scanner(fakedInput);

        professore.menuAppelli(sc);

        String output = outContent.toString();
        
        // Verifichiamo che scatti il catch del NumberFormatException
        assertTrue(output.contains("Errore: Inserisci un valore valido."));
    }

    @Test
    public void testMenuPersonale_GestioneBachecaEInvia() {
        // Entra nel menu appelli (1), prova ad aggiungere (2), esce dalla bacheca (0), logout (0)
        String fakedInput = "1\n2\n0\n0\n";
        Scanner sc = new Scanner(fakedInput);

        professore.menuPersonale(sc);

        String output = outContent.toString();
        
        assertTrue(output.contains("--- MENU PROFESSORE ---"));
        assertTrue(output.contains("Procedura di aggiunta appello avviata."));
        assertTrue(output.contains("Ritorno al menu principale..."));
    }
}