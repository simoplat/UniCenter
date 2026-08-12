package it.project.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.Appello;
import it.project.PianoDiStudi;
import it.project.Studente;

@ExtendWith(MockitoExtension.class)
class PianoStudiValidatorTest {

    @Mock
    private Appello appello;

    @Mock
    private PianoDiStudi pianoDiStudi;

    @Mock
    private IscrizioneValidator nextValidator;

    private PianoStudiValidator validator;
    private Studente studente;

    @BeforeEach
    void setUp() {
        validator = new PianoStudiValidator();
        studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001",
                "Ingegneria Informatica");
    }

    @Test
    void validate_materiaPresenteSenzaNext_ritornaTrue() throws Exception {
        studente.setPianoStudi(pianoDiStudi);
        when(pianoDiStudi.contieneMateria("IS01")).thenReturn(true);
        when(appello.getCodiceMateria()).thenReturn("IS01");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_materiaPresenteConNext_delegaAlProssimoValidator() throws Exception {
        studente.setPianoStudi(pianoDiStudi);
        when(pianoDiStudi.contieneMateria("IS01")).thenReturn(true);
        when(appello.getCodiceMateria()).thenReturn("IS01");
        when(nextValidator.validate(studente, appello)).thenReturn(true);
        validator.setNext(nextValidator);

        assertTrue(validator.validate(studente, appello));
        verify(nextValidator, times(1)).validate(studente, appello);
    }

    @Test
    void validate_materiaAssenteNelPianoStudi_lanciaIllegalStateException() {
        studente.setPianoStudi(pianoDiStudi);
        when(pianoDiStudi.contieneMateria("BD01")).thenReturn(false);
        when(appello.getCodiceMateria()).thenReturn("BD01");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("materia non presente"));
    }

    @Test
    void validate_pianoStudiNullo_lanciaIllegalStateException() {
        studente.setPianoStudi(null);

        assertThrows(IllegalStateException.class, () -> validator.validate(studente, appello));
    }

    @Test
    void validate_materiaAssente_nonDelegaAlProssimoValidator() {
        studente.setPianoStudi(pianoDiStudi);
        when(pianoDiStudi.contieneMateria("BD01")).thenReturn(false);
        when(appello.getCodiceMateria()).thenReturn("BD01");
        validator.setNext(nextValidator);

        assertThrows(IllegalStateException.class, () -> validator.validate(studente, appello));
        verifyNoInteractions(nextValidator);
    }
}
