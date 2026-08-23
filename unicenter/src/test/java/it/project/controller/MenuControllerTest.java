package it.project.controller;

import static org.junit.jupiter.api.Assertions.*;
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

    private Unicenter testUnicenter;

    @Mock
    private UniCenterView viewMock;

    private MenuController menuController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUnicenter = mock(Unicenter.class);
        menuController = spy(new MenuController(testUnicenter, viewMock));
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
        when(testUnicenter.esisteUtente(emailTest)).thenReturn(false);

        // Act
        menuController.loginUtente();

        // Assert
        verify(viewMock).mostraMessaggio("Email non registrata. Riprova.");
        verify(testUnicenter, never()).passwordCorretta(anyString(), anyString());
    }

    @Test
    @DisplayName("Login fallisce se la password è errata")
    void testLoginUtente_PasswordErrata() {
        // Arrange
        String emailTest = "studente@example.com";
        String passTest = "errata";

        doReturn(emailTest).when(menuController).leggiStringa("Inserisci email: ");
        doReturn(passTest).when(menuController).leggiStringa("Inserisci password: ");

        when(testUnicenter.esisteUtente(emailTest)).thenReturn(true);
        when(testUnicenter.passwordCorretta(emailTest, passTest)).thenReturn(false);

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
    void testGestisciImmatricolazione_DataNonValida() throws Exception {
        // Arrange
        String messaggioErrore = "La finestra temporale per le immatricolazioni è chiusa.";
        when(testUnicenter.validaDataImmatricolazione()).thenThrow(new DataNonValidaException(messaggioErrore));

        // Act
        menuController.gestisciImmatricolazione();

        // Assert
        verify(viewMock).mostraErrore(messaggioErrore);
    }
}