package it.project.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.Materia;
import it.project.PianoDiStudi;
import it.project.Studente;
import it.project.controller.GestioneCorsiLaureaController;

class AnnoCorsoMateriaValidatorTest {

    private GestioneCorsiLaureaController corsiController;
    private CorsoDiLaurea corso;
    private AnnoCorsoMateriaValidator validator;

    @BeforeEach
    void setUp() {
        corsiController = mock(GestioneCorsiLaureaController.class);
        corso = new CorsoDiLaurea("ING-INF", "Ingegneria Informatica", "Triennale", 3);

        Materia m1 = new Materia("IS01", "Ingegneria del Software", 6);
        Materia m2 = new Materia("BD01", "Basi di Dati", 6);
        Materia m3 = new Materia("SIC01", "Sicurezza Informatica", 6);

        corso.aggiungiMateriaAdAnno(1, m1);
        corso.aggiungiMateriaAdAnno(2, m2);
        corso.aggiungiMateriaAdAnno(3, m3);
        corso.finalizza();

        when(corsiController.trovaCorsoDiLaureaById("ING-INF")).thenReturn(corso);
        validator = new AnnoCorsoMateriaValidator(corsiController);
    }

    private Studente creaStudente(int annoCorrente) {
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.getCarriera().setAnnoCorrente(annoCorrente);
        return studente;
    }

    @Test
    void validate_materiaObbligatoria_stessoAnno_ritornaTrue() throws Exception {
        Studente studente = creaStudente(1); // 1° anno
        studente.getPianoDiStudi().aggiungiMateriaObbligatoria("IS01"); // materia del 1° anno

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("IS01");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_materiaObbligatoria_annoPrecedente_ritornaTrue() throws Exception {
        Studente studente = creaStudente(2); // 2° anno
        studente.getPianoDiStudi().aggiungiMateriaObbligatoria("IS01"); // materia arretrata del 1° anno

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("IS01");

        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_materiaObbligatoria_annoFuturo_lanciaIllegalStateException() {
        Studente studente = creaStudente(1); // 1° anno
        studente.getPianoDiStudi().aggiungiMateriaObbligatoria("BD01"); // materia del 2° anno

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("BD01");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> validator.validate(studente, appello));
        assertTrue(ex.getMessage().contains("materia obbligatoria 'BD01'"));
        assertTrue(ex.getMessage().contains("2° anno"));
        assertTrue(ex.getMessage().contains("1° anno"));
    }

    @Test
    void validate_materiaAScelta_annoFuturo_controlloNonApplicato_ritornaTrue() throws Exception {
        Studente studente = creaStudente(1); // 1° anno
        // Materia inserita come materia a scelta
        studente.getPianoDiStudi().aggiungiMateriaAScelta("BD01");

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("BD01");

        // Il controllo non si applica per le materie a scelta: deve passare con successo
        assertTrue(validator.validate(studente, appello));
    }

    @Test
    void validate_collegamentoSuccessivo_invocaNextValidator() throws Exception {
        IscrizioneValidator next = mock(IscrizioneValidator.class);
        when(next.validate(any(), any())).thenReturn(true);
        validator.setNext(next);

        Studente studente = creaStudente(1);
        studente.getPianoDiStudi().aggiungiMateriaObbligatoria("IS01");

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("IS01");

        assertTrue(validator.validate(studente, appello));
        verify(next, times(1)).validate(studente, appello);
    }
}
