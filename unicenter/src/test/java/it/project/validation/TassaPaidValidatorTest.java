package it.project.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.Appello;
import it.project.Carriera;
import it.project.Studente;
import it.project.exceptions.validator.TasseNonPagateException;

@ExtendWith(MockitoExtension.class)
class TassaPaidValidatorTest {

    @Mock
    private Appello appello;

    @Mock
    private Carriera carriera;

    @Mock
    private IscrizioneValidator nextValidator;

    private TassaPaidValidator validator;
    private Studente studente;

    @BeforeEach
    void setUp() {
        validator = new TassaPaidValidator();
        studente = new Studente("M002", "Luigi", "Verdi", "luigi@studenti.it", "pass123", "CF002",
                "Ingegneria Informatica");
    }

    @Test
    void validate_tassePagate_ritornaTrue() throws Exception {
        studente.setTassePagate(true);

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_tasseNonPagate_lanciaTasseNonPagateException() {
        studente.setTassePagate(false);

        TasseNonPagateException ex = assertThrows(TasseNonPagateException.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("tasse universitarie non saldate"));
    }

    @Test
    void validate_tassePagate_delegaAlProssimoValidator() throws Exception {
        studente.setTassePagate(true);
        when(nextValidator.validate(studente, appello)).thenReturn(true);
        validator.setNext(nextValidator);

        assertTrue(validator.validate(studente, appello));
        verify(nextValidator, times(1)).validate(studente, appello);
    }

    @Test
    void validate_tasseNonPagate_nonDelegaAlProssimoValidator() {
        studente.setTassePagate(false);
        validator.setNext(nextValidator);

        assertThrows(TasseNonPagateException.class, () -> validator.validate(studente, appello));
        verifyNoInteractions(nextValidator);
    }
}
