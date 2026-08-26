package it.project.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.PostiNonValidi;

@ExtendWith(MockitoExtension.class)
class PostiDisponibiliValidatorTest {

    @Mock
    private Appello appello;

    @Mock
    private Studente studente;

    @Mock
    private IscrizioneValidator nextValidator;

    private PostiDisponibiliValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PostiDisponibiliValidator();
    }

    @Test
    void validate_postiDisponibili_ritornaTrue() throws Exception {
        when(appello.getPostiDisponibili()).thenReturn(10);

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_unSoloPostoDisponibile_ritornaTrue() throws Exception {
        when(appello.getPostiDisponibili()).thenReturn(1);

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_postiEsauriti_lanciaPostiNonValidi() {
        when(appello.getPostiDisponibili()).thenReturn(0);

        PostiNonValidi ex = assertThrows(PostiNonValidi.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("posti esauriti"));
    }

    @Test
    void validate_postiNegativi_lanciaPostiNonValidi() {
        when(appello.getPostiDisponibili()).thenReturn(-1);

        assertThrows(PostiNonValidi.class, () -> validator.validate(studente, appello));
    }

    @Test
    void validate_postiDisponibili_delegaAlProssimoValidator() throws Exception {
        when(appello.getPostiDisponibili()).thenReturn(5);
        when(nextValidator.validate(studente, appello)).thenReturn(true);
        validator.setNext(nextValidator);

        assertTrue(validator.validate(studente, appello));
        verify(nextValidator, times(1)).validate(studente, appello);
    }

    @Test
    void validate_postiEsauriti_nonDelegaAlProssimoValidator() {
        when(appello.getPostiDisponibili()).thenReturn(0);
        validator.setNext(nextValidator);

        assertThrows(PostiNonValidi.class, () -> validator.validate(studente, appello));
        verifyNoInteractions(nextValidator);
    }
}
