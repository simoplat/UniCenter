package it.project.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.DataNonValidaException;

@ExtendWith(MockitoExtension.class)
class DataTermineIscrizioneValidatorTest {

    @Mock
    private Appello appello;

    @Mock
    private IscrizioneValidator nextValidator;

    private DataTermineIscrizioneValidator validator;
    private Studente studente;

    @BeforeEach
    void setUp() {
        validator = new DataTermineIscrizioneValidator();
        studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001",
                "Ingegneria Informatica");
    }

    @Test
    void validate_termineIscrizioneNullo_lanciaDataNonValidaException() {
        when(appello.getTermineIscrizione()).thenReturn(null);

        DataNonValidaException ex = assertThrows(DataNonValidaException.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("data di scadenza iscrizioni non definita"));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        it.project.database.ClockProvider.resetClock();
    }

    @Test
    void validate_oggiDopoIlTermine_lanciaDataNonValidaException() {
        LocalDate termine = LocalDate.of(2026, 1, 1);
        LocalDate oggi = LocalDate.of(2026, 1, 2);
        when(appello.getTermineIscrizione()).thenReturn(termine);

        it.project.database.ClockProvider.setFixedDate(oggi);

        DataNonValidaException ex = assertThrows(DataNonValidaException.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("si sono chiuse il"));
    }

    @Test
    void validate_oggiUgualeAlTermine_ritornaTrue() throws Exception {
        LocalDate termine = LocalDate.of(2026, 1, 1);
        when(appello.getTermineIscrizione()).thenReturn(termine);

        it.project.database.ClockProvider.setFixedDate(termine);

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_oggiPrimaDelTermine_ritornaTrue() throws Exception {
        LocalDate termine = LocalDate.of(2026, 1, 10);
        LocalDate oggi = LocalDate.of(2026, 1, 5);
        when(appello.getTermineIscrizione()).thenReturn(termine);

        it.project.database.ClockProvider.setFixedDate(oggi);

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_dataValida_delegaAlProssimoValidator() throws Exception {
        LocalDate termine = LocalDate.of(2026, 1, 10);
        LocalDate oggi = LocalDate.of(2026, 1, 5);
        when(appello.getTermineIscrizione()).thenReturn(termine);
        when(nextValidator.validate(studente, appello)).thenReturn(true);
        validator.setNext(nextValidator);

        it.project.database.ClockProvider.setFixedDate(oggi);

        assertTrue(validator.validate(studente, appello));
        verify(nextValidator, times(1)).validate(studente, appello);
    }

    @Test
    void validate_dataScaduta_nonDelegaAlProssimoValidator() {
        LocalDate termine = LocalDate.of(2026, 1, 1);
        LocalDate oggi = LocalDate.of(2026, 1, 2);
        when(appello.getTermineIscrizione()).thenReturn(termine);
        validator.setNext(nextValidator);

        it.project.database.ClockProvider.setFixedDate(oggi);

        assertThrows(DataNonValidaException.class, () -> validator.validate(studente, appello));

        verifyNoInteractions(nextValidator);
    }
}
