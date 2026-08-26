package it.project.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.validator.IscrizioneNonValidaException;

@ExtendWith(MockitoExtension.class)
class AbstractIscrizioneValidatorTest {

    private static class DummyValidator extends AbstractIscrizioneValidator {
        @Override
        public boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException {
            return checkNext(studente, appello);
        }
    }

    @Mock
    private Appello appello;

    @Mock
    private IscrizioneValidator nextValidator;

    private DummyValidator dummyValidator;
    private Studente studente;

    @BeforeEach
    void setUp() {
        dummyValidator = new DummyValidator();
        studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "Ingegneria Informatica");
    }

    @Test
    @DisplayName("checkNext() senza next validator ritorna true")
    void testCheckNextSenzaNextRitornaTrue() throws Exception {
        assertTrue(dummyValidator.validate(studente, appello));
    }

    @Test
    @DisplayName("checkNext() con next validator delega correttamente la validazione")
    void testCheckNextConNextDelega() throws Exception {
        when(nextValidator.validate(studente, appello)).thenReturn(true);
        dummyValidator.setNext(nextValidator);

        assertTrue(dummyValidator.validate(studente, appello));
        verify(nextValidator, times(1)).validate(studente, appello);
    }

    @Test
    @DisplayName("checkNext() propaga eccezioni sollevate dal validatore successivo")
    void testCheckNextPropagaEccezione() throws Exception {
        when(nextValidator.validate(studente, appello)).thenThrow(new IllegalStateException("Errore successivo"));
        dummyValidator.setNext(nextValidator);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dummyValidator.validate(studente, appello));
        assertEquals("Errore successivo", ex.getMessage());
    }
}
