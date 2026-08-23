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

    private TestUnicenter testUnicenter;

    @Mock
    private UniCenterView viewMock;

    private MenuController menuController;

    static class TestUnicenter extends Unicenter {
        boolean esisteUtenteResult = false;
        boolean passwordCorrettaResult = false;
        boolean passwordCorrettaCalled = false;
        boolean validaDataThrows = false;
        String validaDataErrorMsg = "";

        @Override
        public boolean esisteUtente(String email) {
            return esisteUtenteResult;
        }

        @Override
        public boolean passwordCorretta(String email, String password) {
            passwordCorrettaCalled = true;
            return passwordCorrettaResult;
        }

        @Override
        public boolean validaDataImmatricolazione() throws DataNonValidaException {
            if (validaDataThrows) {
                throw new DataNonValidaException(validaDataErrorMsg);
            }
            return true;
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUnicenter = new TestUnicenter();
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
        testUnicenter.esisteUtenteResult = false;

        // Act
        menuController.loginUtente();

        // Assert
        verify(viewMock).mostraMessaggio("Email non registrata. Riprova.");
        assertFalse(testUnicenter.passwordCorrettaCalled);
    }

    @Test
    @DisplayName("Login fallisce se la password è errata")
    void testLoginUtente_PasswordErrata() {
        // Arrange
        String emailTest = "studente@example.com";
        String passTest = "errata";

        doReturn(emailTest).when(menuController).leggiStringa("Inserisci email: ");
        doReturn(passTest).when(menuController).leggiStringa("Inserisci password: ");

        testUnicenter.esisteUtenteResult = true;
        testUnicenter.passwordCorrettaResult = false;

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
    void testGestisciImmatricolazione_DataNonValida() {
        // Arrange
        String messaggioErrore = "La finestra temporale per le immatricolazioni è chiusa.";
        testUnicenter.validaDataThrows = true;
        testUnicenter.validaDataErrorMsg = messaggioErrore;

        // Act
        menuController.gestisciImmatricolazione();

        // Assert
        verify(viewMock).mostraErrore(messaggioErrore);
    }
}