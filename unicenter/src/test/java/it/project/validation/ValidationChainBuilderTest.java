package it.project.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import it.project.Appello;
import it.project.PianoDiStudi;
import it.project.Studente;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.validator.PianoStudiNonValidoException;
import it.project.exceptions.validator.TasseNonPagateException;

class ValidationChainBuilderTest {

    private Studente creaStudente() {
        return new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001",
                "Ingegneria Informatica");
    }

    // ---------------------------------------------------------------
    // Comportamento del builder (collegamento dei validator)
    // ---------------------------------------------------------------

    @Test
    void build_senzaValidatori_lanciaIllegalStateException() {
        ValidationChainBuilder builder = new ValidationChainBuilder();

        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    void build_conUnSoloValidatore_ritornaQuelValidatoreSenzaCollegamenti() {
        IscrizioneValidator v1 = mock(IscrizioneValidator.class);
        ValidationChainBuilder builder = new ValidationChainBuilder();

        IscrizioneValidator risultato = builder.addValidator(v1).build();

        assertSame(v1, risultato);
        verify(v1, never()).setNext(any());
    }

    @Test
    void build_conPiuValidatori_liCollegaInSequenzaCorretta() {
        IscrizioneValidator v1 = mock(IscrizioneValidator.class);
        IscrizioneValidator v2 = mock(IscrizioneValidator.class);
        IscrizioneValidator v3 = mock(IscrizioneValidator.class);

        ValidationChainBuilder builder = new ValidationChainBuilder();
        IscrizioneValidator risultato = builder.addValidator(v1).addValidator(v2).addValidator(v3).build();

        assertSame(v1, risultato);
        verify(v1, times(1)).setNext(v2);
        verify(v2, times(1)).setNext(v3);
        verify(v3, never()).setNext(any());
    }

    @Test
    void addValidator_ritornaSempreLaStessaIstanzaDelBuilder_perConsentireIlChaining() {
        ValidationChainBuilder builder = new ValidationChainBuilder();
        IscrizioneValidator v1 = mock(IscrizioneValidator.class);

        ValidationChainBuilder risultato = builder.addValidator(v1);

        assertSame(builder, risultato);
    }

    // ---------------------------------------------------------------
    // Ordine della catena di default (buildDefaultChain)
    // ---------------------------------------------------------------

    @Test
    void buildDefaultChain_primoAnelloEPianoStudi_falliscePrimaDeiControlliSuccessivi() {
        // Il piano di studi reale creato dal costruttore di Studente non è null,
        // ma è vuoto: non contiene la materia richiesta. Deve fallire qui,
        // senza raggiungere i controlli successivi (posti/tasse/cognome/data),
        // a conferma che PianoStudiValidator è il primo anello della catena.
        IscrizioneValidator chain = ValidationChainBuilder.buildDefaultChain();

        Studente studente = creaStudente(); // piano di studi vuoto di default
        studente.setTassePagate(false); // condizione che farebbe fallire anche i validator successivi

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("IS01");

        assertThrows(PianoStudiNonValidoException.class, () -> chain.validate(studente, appello));

        verify(appello, never()).getPostiDisponibili();
        verify(appello, never()).getVincoloLetteraCognome();
        verify(appello, never()).getTermineIscrizione();
    }

    @Test
    void buildDefaultChain_pianoStudiOkMaTasseNonPagate_falliscePrimaDiPostiCognomeEData() throws Exception {
        IscrizioneValidator chain = ValidationChainBuilder.buildDefaultChain();

        Studente studente = creaStudente();
        PianoDiStudi pianoDiStudi = mock(PianoDiStudi.class);
        when(pianoDiStudi.contieneMateria("IS01")).thenReturn(true);
        studente.setPianoDiStudi(pianoDiStudi);
        studente.setTassePagate(false);

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("IS01");
        when(appello.getPostiDisponibili()).thenReturn(10);

        TasseNonPagateException ex = assertThrows(TasseNonPagateException.class,
                () -> chain.validate(studente, appello));
        assertTrue(ex.getMessage().contains("tasse universitarie non saldate"));

        // getVincoloLetteraCognome e getTermineIscrizione non devono essere consultati:
        // la catena si è fermata prima di arrivare a CognomeFasciaValidator/DataTermineIscrizioneValidator
        verify(appello, never()).getVincoloLetteraCognome();
        verify(appello, never()).getTermineIscrizione();
    }

    @Test
    void buildDefaultChain_ultimoAnelloEData_vieneRaggiuntoSoloSeTuttiIPrecedentiPassano() {
        IscrizioneValidator chain = ValidationChainBuilder.buildDefaultChain();

        Studente studente = creaStudente();
        PianoDiStudi pianoDiStudi = mock(PianoDiStudi.class);
        when(pianoDiStudi.contieneMateria("IS01")).thenReturn(true);
        studente.setPianoDiStudi(pianoDiStudi);
        studente.setTassePagate(true);

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("IS01");
        when(appello.getPostiDisponibili()).thenReturn(10);
        when(appello.getVincoloLetteraCognome()).thenReturn("A-Z");
        when(appello.getTermineIscrizione()).thenReturn(LocalDate.now().minusDays(1)); // termine scaduto

        DataNonValidaException ex = assertThrows(DataNonValidaException.class,
                () -> chain.validate(studente, appello));
        assertTrue(ex.getMessage().contains("si sono chiuse il"));
    }

    @Test
    void buildDefaultChain_tuttiIControlliSuperati_ritornaTrue() throws Exception {
        IscrizioneValidator chain = ValidationChainBuilder.buildDefaultChain();

        Studente studente = creaStudente();
        PianoDiStudi pianoDiStudi = mock(PianoDiStudi.class);
        when(pianoDiStudi.contieneMateria("IS01")).thenReturn(true);
        studente.setPianoDiStudi(pianoDiStudi);
        studente.setTassePagate(true);

        Appello appello = mock(Appello.class);
        when(appello.getCodiceMateria()).thenReturn("IS01");
        when(appello.getPostiDisponibili()).thenReturn(10);
        when(appello.getVincoloLetteraCognome()).thenReturn("A-Z");
        when(appello.getTermineIscrizione()).thenReturn(LocalDate.now().plusDays(5));

        assertTrue(chain.validate(studente, appello));
    }
}