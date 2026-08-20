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
import it.project.EsameSostenuto;
import it.project.Libretto;
import it.project.Studente;

@ExtendWith(MockitoExtension.class)
class EsameSuperatoValidatorTest {

    @Mock
    private Appello appello;

    @Mock
    private IscrizioneValidator nextValidator;

    private EsameSuperatoValidator validator;
    private Studente studente;

    @BeforeEach
    void setUp() {
        validator = new EsameSuperatoValidator();
        studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "Ingegneria Informatica");
    }

    @Test
    @DisplayName("validate() ritorna true se l'esame non è superato e non c'è un next validator")
    void validate_esameNonSuperatoSenzaNext_ritornaTrue() throws Exception {
        when(appello.getCodiceMateria()).thenReturn("IS01");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    @DisplayName("validate() delega al prossimo validatore se l'esame non è superato")
    void validate_esameNonSuperatoConNext_delegaAlProssimoValidator() throws Exception {
        when(appello.getCodiceMateria()).thenReturn("IS01");
        when(nextValidator.validate(studente, appello)).thenReturn(true);
        validator.setNext(nextValidator);

        assertTrue(validator.validate(studente, appello));
        verify(nextValidator, times(1)).validate(studente, appello);
    }

    @Test
    @DisplayName("validate() lancia IllegalStateException se l'esame è già superato nel libretto")
    void validate_esameGiaSuperatoNelLibretto_lanciaIllegalStateException() {
        when(appello.getCodiceMateria()).thenReturn("IS01");

        // Registriamo l'esame approvato nel libretto dello studente
        EsameSostenuto esame = new EsameSostenuto("ESM-01", "APP-01", studente.getMatricola(), "IS01", "1", 30, false, 9, 7);
        esame.accetta();
        studente.getLibretto().registraEsame(esame);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("esame già superato"));
    }

    @Test
    @DisplayName("validate() non delega al prossimo validatore se l'esame è già superato")
    void validate_esameGiaSuperato_nonDelegaAlProssimoValidator() {
        when(appello.getCodiceMateria()).thenReturn("IS01");
        validator.setNext(nextValidator);

        EsameSostenuto esame = new EsameSostenuto("ESM-01", "APP-01", studente.getMatricola(), "IS01", "1", 28, false, 9, 7);
        esame.accetta();
        studente.getLibretto().registraEsame(esame);

        assertThrows(IllegalStateException.class, () -> validator.validate(studente, appello));
        verifyNoInteractions(nextValidator);
    }

    @Test
    @DisplayName("validate() ritorna true se il libretto è vuoto")
    void validate_librettoVuoto_ritornaTrue() throws Exception {
        when(appello.getCodiceMateria()).thenReturn("BD01");

        assertTrue(validator.validate(studente, appello));
    }
}
