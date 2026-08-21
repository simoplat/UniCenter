package it.project.controller;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.project.Unicenter;
import it.project.exceptions.DataNonValidaException;
import it.project.view.UniCenterView;

@DisplayName("Test Unitari - MenuController")
class MenuControllerTest {

    @Mock
    private Unicenter unicenterMock;

    @Mock
    private UniCenterView viewMock;

    private MenuController menuController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        menuController = spy(new MenuController(unicenterMock, viewMock));
    }

    // ==========================================
    // TEST LOGIN
    // ==========================================

    @Test
    @DisplayName("Login fallisce se l'email non esiste")
    void testLoginUtente_EmailNonRegistrata() {
        // Arrange
        String emailTest = "inesistente@example.com";
        doReturn(emailTest).when(menuController).leggiStringa("Inserisci email: ");
        when(unicenterMock.esisteUtente(emailTest)).thenReturn(false);

        // Act
        menuController.loginUtente();

        // Assert
        verify(viewMock).mostraMessaggio("Email non registrata. Riprova.");
        verify(unicenterMock, never()).passwordCorretta(anyString(), anyString());
    }

    @Test
    @DisplayName("Login fallisce se la password è errata")
    void testLoginUtente_PasswordErrata() {
        // Arrange
        String emailTest = "studente@example.com";
        String passTest = "errata";

        doReturn(emailTest).when(menuController).leggiStringa("Inserisci email: ");
        doReturn(passTest).when(menuController).leggiStringa("Inserisci password: ");

        when(unicenterMock.esisteUtente(emailTest)).thenReturn(true);
        when(unicenterMock.passwordCorretta(emailTest, passTest)).thenReturn(false);

        // Act
        menuController.loginUtente();

        // Assert
        verify(viewMock).mostraMessaggio("Password errata. Riprova.");
    }

    // ==========================================
    // TEST IMMATRICOLAZIONE
    // ==========================================

    @Test
    @DisplayName("Immatricolazione bloccata per finestra temporale chiusa")
    void testGestisciImmatricolazione_DataNonValida() throws DataNonValidaException {
        // Arrange
        String messaggioErrore = "La finestra temporale per le immatricolazioni è chiusa.";
        doThrow(new DataNonValidaException(messaggioErrore)).when(unicenterMock).validaDataImmatricolazione();

        // Act
        menuController.gestisciImmatricolazione();

        // Assert
        verify(viewMock).mostraErrore(messaggioErrore);
    }
}