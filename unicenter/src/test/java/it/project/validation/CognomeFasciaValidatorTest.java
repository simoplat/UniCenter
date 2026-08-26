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
import it.project.exceptions.validator.FasciaCognomeNonValidaException;

@ExtendWith(MockitoExtension.class)
class CognomeFasciaValidatorTest {

    @Mock
    private Appello appello;

    @Mock
    private IscrizioneValidator nextValidator;

    private CognomeFasciaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CognomeFasciaValidator();
    }

    private Studente creaStudente(String cognome) {
        return new Studente("M001", "Nome", cognome, "email@studenti.it", "pass123", "CF001",
                "Ingegneria Informatica");
    }

    @Test
    void validate_fasciaNulla_nonEsegueControlloERitornaTrue() throws Exception {
        Studente studente = creaStudente("Rossi");
        when(appello.getVincoloLetteraCognome()).thenReturn(null);

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_fasciaVuota_nonEsegueControlloERitornaTrue() throws Exception {
        Studente studente = creaStudente("Rossi");
        when(appello.getVincoloLetteraCognome()).thenReturn("   ");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_cognomeDentroLaFascia_ritornaTrue() throws Exception {
        Studente studente = creaStudente("Rossi"); // R
        when(appello.getVincoloLetteraCognome()).thenReturn("A-Z");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_cognomeSopraLaFascia_lanciaFasciaCognomeNonValidaException() {
        Studente studente = creaStudente("Rossi"); // R
        when(appello.getVincoloLetteraCognome()).thenReturn("A-M");

        FasciaCognomeNonValidaException ex = assertThrows(FasciaCognomeNonValidaException.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("A-M"));
    }

    @Test
    void validate_cognomeSottoLaFascia_lanciaFasciaCognomeNonValidaException() {
        Studente studente = creaStudente("Bianchi"); // B
        when(appello.getVincoloLetteraCognome()).thenReturn("N-Z");

        assertThrows(FasciaCognomeNonValidaException.class, () -> validator.validate(studente, appello));
    }

    @Test
    void validate_cognomeSulLimiteInferioreDellaFascia_ritornaTrue() throws Exception {
        Studente studente = creaStudente("Rossi"); // R, limite inferiore incluso
        when(appello.getVincoloLetteraCognome()).thenReturn("R-Z");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_cognomeSulLimiteSuperioreDellaFascia_ritornaTrue() throws Exception {
        Studente studente = creaStudente("Rossi"); // R, limite superiore incluso
        when(appello.getVincoloLetteraCognome()).thenReturn("A-R");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_fasciaCaseInsensitive_ritornaTrue() throws Exception {
        Studente studente = creaStudente("rossi"); // minuscolo
        when(appello.getVincoloLetteraCognome()).thenReturn("a-z");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_cognomeNullo_lanciaFasciaCognomeNonValidaException() {
        Studente studente = creaStudente("Placeholder");
        studente.setCognome(null);
        when(appello.getVincoloLetteraCognome()).thenReturn("A-Z");

        assertThrows(FasciaCognomeNonValidaException.class, () -> validator.validate(studente, appello));
    }

    @Test
    void validate_cognomeVuoto_lanciaFasciaCognomeNonValidaException() {
        Studente studente = creaStudente("Placeholder");
        studente.setCognome("   ");
        when(appello.getVincoloLetteraCognome()).thenReturn("A-Z");

        assertThrows(FasciaCognomeNonValidaException.class, () -> validator.validate(studente, appello));
    }

    @Test
    void validate_fasciaMalformataSenzaTrattino_vieneIgnorataERitornaTrue() throws Exception {
        Studente studente = creaStudente("Rossi");
        when(appello.getVincoloLetteraCognome()).thenReturn("AZ");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_fasciaConTroppiTrattini_vieneIgnorataERitornaTrue() throws Exception {
        Studente studente = creaStudente("Rossi");
        when(appello.getVincoloLetteraCognome()).thenReturn("A-M-Z");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_cognomeDentroLaFascia_delegaAlProssimoValidator() throws Exception {
        Studente studente = creaStudente("Rossi");
        when(appello.getVincoloLetteraCognome()).thenReturn("A-Z");
        when(nextValidator.validate(studente, appello)).thenReturn(true);
        validator.setNext(nextValidator);

        assertTrue(validator.validate(studente, appello));
        verify(nextValidator, times(1)).validate(studente, appello);
    }

    @Test
    void validate_cognomeFuoriFascia_nonDelegaAlProssimoValidator() {
        Studente studente = creaStudente("Rossi");
        when(appello.getVincoloLetteraCognome()).thenReturn("A-M");
        validator.setNext(nextValidator);

        assertThrows(FasciaCognomeNonValidaException.class, () -> validator.validate(studente, appello));
        verifyNoInteractions(nextValidator);
    }
}
