package it.project.controller;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import it.project.ConsoleUI;
import it.project.Unicenter;
import it.project.exceptions.DataNonValidaException;

@DisplayName("Test Unitari - MenuController")
class MenuControllerTest {

    @Mock
    private Unicenter unicenterMock;

    @Mock
    private ConsoleUI consoleUIMock;

    private MockedStatic<ConsoleUI> consoleUIStaticMock;
    private MenuController menuController;

    @BeforeEach
    void setUp() {
        // Inizializza i mock annotati
        MockitoAnnotations.openMocks(this);

        // Mock del Singleton ConsoleUI usato internamente da MenuController
        consoleUIStaticMock = mockStatic(ConsoleUI.class);
        consoleUIStaticMock.when(ConsoleUI::getInstance).thenReturn(consoleUIMock);

        // Ora possiamo istanziare in sicurezza il controller iniettando Unicenter
        menuController = new MenuController(unicenterMock);
    }

    @AfterEach
    void tearDown() {
        // Fondamentale chiudere il mock statico per evitare conflitti tra i test
        consoleUIStaticMock.close();
    }

    // ==========================================
    // TEST LOGIN
    // ==========================================

    @Test
    @DisplayName("Login fallisce se l'email non esiste")
    void testLoginUtente_EmailNonRegistrata() {
        // Arrange
        String emailTest = "inesistente@example.com";
        when(consoleUIMock.leggiStringa("Inserisci email: ")).thenReturn(emailTest);
        when(unicenterMock.esisteUtente(emailTest)).thenReturn(false);

        // Act
        menuController.loginUtente();

        // Assert
        verify(consoleUIMock).mostraMessaggio("Email non registrata. Riprova.");
        verify(unicenterMock, never()).passwordCorretta(anyString(), anyString());
    }

    @Test
    @DisplayName("Login fallisce se la password è errata")
    void testLoginUtente_PasswordErrata() {
        // Arrange
        String emailTest = "studente@example.com";
        String passTest = "errata";
        
        when(consoleUIMock.leggiStringa("Inserisci email: ")).thenReturn(emailTest);
        when(consoleUIMock.leggiStringa("Inserisci password: ")).thenReturn(passTest);
        
        when(unicenterMock.esisteUtente(emailTest)).thenReturn(true);
        when(unicenterMock.passwordCorretta(emailTest, passTest)).thenReturn(false);

        // Act
        menuController.loginUtente();

        // Assert
        verify(consoleUIMock).mostraMessaggio("Password errata. Riprova.");
    }

    // ==========================================
    // TEST IMMATRICOLAZIONE
    // ==========================================

    @Test
    @DisplayName("Immatricolazione bloccata per finestra temporale chiusa")
    void testGestisciImmatricolazione_DataNonValida() throws DataNonValidaException {
        // Arrange
        String messaggioErrore = "La finestra temporale per le immatricolazioni è chiusa.";
        
        // Simula il lancio dell'eccezione quando viene validata la data
        doThrow(new DataNonValidaException(messaggioErrore)).when(unicenterMock).validaDataImmatricolazione();

        // Act
        // Il metodo gestisciImmatricolazione() non solleva l'eccezione ma la cattura e la stampa
        try {
            // Nota: Poiché gestisciImmatricolazione è privato nella classe originale, 
            // occorre renderlo 'package-private' (togliendo 'private') o testarlo tramite la chiamata generica del menu. 
            // In Java, una buona pratica è modificare la visibilità a 'protected' o package-private per i test.
            // Per questo esempio, usiamo la Reflection per accedere al metodo se non si vuole modificare la sorgente.
            java.lang.reflect.Method method = MenuController.class.getDeclaredMethod("gestisciImmatricolazione");
            method.setAccessible(true);
            method.invoke(menuController);
            
        } catch (Exception e) {
            // L'eccezione è gestita all'interno del metodo richiamato tramite reflection
        }

        // Assert
        // Verifica che il controller abbia intercettato l'eccezione e mostrato l'errore sulla ConsoleUI
        verify(consoleUIMock).mostraErrore(messaggioErrore);
    }
}