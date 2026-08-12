package it.project.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.CorsoDiLaurea;
import it.project.Studente;
import it.project.Unicenter;
import it.project.exceptions.DataNonValidaException;
import it.project.strategy.ICalcoloTasseStrategy;

/**
 * Test unitari per {@link ImmatricolazioneController}.
 *
 * Dipendenze Maven necessarie (oltre a junit-jupiter):
 *   - org.mockito:mockito-core:5.x   (include l'inline mock maker, necessario per mockStatic)
 *   - org.mockito:mockito-junit-jupiter:5.x
 */
@ExtendWith(MockitoExtension.class)
class ImmatricolazioneControllerTest {

    @Mock
    private Unicenter unicenter;

    @Mock
    private CorsoDiLaurea corsoDiLaureaMock;

    private ImmatricolazioneController controller;

    private static final String NOME = "Mario";
    private static final String COGNOME = "Rossi";
    private static final String EMAIL = "mario.rossi@studenti.it";
    private static final String PASSWORD = "pass123";
    private static final String CORSO = "Ingegneria Informatica";
    private static final double TASSA_BASE = 500.0;
    private static final String CODICE_FISCALE = "CODICEFISCALEMARIOROSSI";

    @BeforeEach
    void setUp() {
        controller = new ImmatricolazioneController(unicenter);
    }

    // ---------------------------------------------------------------
    // immatricolaStudente
    // ---------------------------------------------------------------

    @Test
    void immatricolaStudente_corsoTrovato_creaStudenteConDatiCorretti() {
        when(unicenter.trovaCorsoDiLaureaByNome(CORSO)).thenReturn(corsoDiLaureaMock);

        Studente studente = controller.immatricolaStudente(
                NOME, COGNOME, EMAIL, PASSWORD, CORSO, TASSA_BASE, CODICE_FISCALE);

        assertNotNull(studente);
        assertEquals(NOME, studente.getNome());
        assertEquals(COGNOME, studente.getCognome());
        assertEquals(EMAIL, studente.getEmail());
        assertEquals(CODICE_FISCALE, studente.getCodiceFiscale());
        assertEquals(CORSO, studente.getCorsoDiLaurea());
        assertNotNull(studente.getMatricola());
        assertFalse(studente.isTassePagate(), "Alla creazione le tasse non devono risultare pagate");

        verify(unicenter, times(1)).trovaCorsoDiLaureaByNome(CORSO);
    }

    @Test
    void immatricolaStudente_corsoNonTrovato_lanciaIllegalArgumentException() {
        when(unicenter.trovaCorsoDiLaureaByNome("Corso Inesistente")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.immatricolaStudente(
                        NOME, COGNOME, EMAIL, PASSWORD, "Corso Inesistente", TASSA_BASE, CODICE_FISCALE));

        assertTrue(ex.getMessage().contains("corso non esistente"));
        assertTrue(ex.getMessage().contains("Corso Inesistente"));

        // Il builder non deve nemmeno essere invocato: nessuna ulteriore interazione attesa
        verify(unicenter, times(1)).trovaCorsoDiLaureaByNome("Corso Inesistente");
        verifyNoMoreInteractions(unicenter);
    }

    @Test
    void immatricolaStudente_nomeNonValido_propagaEccezioneDalBuilder() {
        when(unicenter.trovaCorsoDiLaureaByNome(CORSO)).thenReturn(corsoDiLaureaMock);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.immatricolaStudente(
                        "   ", COGNOME, EMAIL, PASSWORD, CORSO, TASSA_BASE, CODICE_FISCALE));

        assertEquals("Il nome non può essere vuoto.", ex.getMessage());
    }

    @Test
    void immatricolaStudente_emailNonValida_propagaEccezioneDalBuilder() {
        when(unicenter.trovaCorsoDiLaureaByNome(CORSO)).thenReturn(corsoDiLaureaMock);

        assertThrows(IllegalArgumentException.class,
                () -> controller.immatricolaStudente(
                        NOME, COGNOME, "email-non-valida", PASSWORD, CORSO, TASSA_BASE, CODICE_FISCALE));
    }

    @Test
    void immatricolaStudente_passwordTroppoCorta_propagaEccezioneDalBuilder() {
        when(unicenter.trovaCorsoDiLaureaByNome(CORSO)).thenReturn(corsoDiLaureaMock);

        assertThrows(IllegalArgumentException.class,
                () -> controller.immatricolaStudente(
                        NOME, COGNOME, EMAIL, "123", CORSO, TASSA_BASE, CODICE_FISCALE));
    }

    @Test
    void immatricolaStudente_usaStrategyPerCalcolareTotaleTasse_conMockIniettataViaReflection() throws Exception {
        when(unicenter.trovaCorsoDiLaureaByNome(CORSO)).thenReturn(corsoDiLaureaMock);

        // La strategy è istanziata internamente dal controller (new CalcoloTasseStandardStrategy()),
        // quindi la sostituiamo via reflection per isolare il test dalla logica di calcolo reale
        // e verificare solo l'interazione.
        ICalcoloTasseStrategy strategyMock = mock(ICalcoloTasseStrategy.class);
        when(strategyMock.calcolaTasse(TASSA_BASE, false)).thenReturn(1234.56);

        Field strategyField = ImmatricolazioneController.class.getDeclaredField("calcoloTasseStrategy");
        strategyField.setAccessible(true);
        strategyField.set(controller, strategyMock);

        Studente studente = controller.immatricolaStudente(
                NOME, COGNOME, EMAIL, PASSWORD, CORSO, TASSA_BASE, CODICE_FISCALE);

        assertEquals(1234.56, studente.getTotaleTasse(), 0.0001);
        verify(strategyMock, times(1)).calcolaTasse(TASSA_BASE, false);
    }

    // ---------------------------------------------------------------
    // validaDataImmatricolazione
    // ---------------------------------------------------------------

    @Test
    void validaDataImmatricolazione_meseAgosto_ritornaTrue() {
        // IMPORTANTE: costruire la data PRIMA di aprire mockStatic. Se si costruisce
        // dentro when(...).thenReturn(LocalDate.of(...)), la chiamata a LocalDate.of()
        // viene valutata come argomento mentre LocalDate è già mockata staticamente e
        // lo stub precedente non è ancora completato -> UnfinishedStubbingException.
        LocalDate dataFissata = LocalDate.of(2026, 8, 1);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFissata);

            assertDoesNotThrow(() -> {
                boolean risultato = controller.validaDataImmatricolazione();
                assertTrue(risultato);
            });
        }
    }

    @Test
    void validaDataImmatricolazione_meseSettembre_ritornaTrue() {
        LocalDate dataFissata = LocalDate.of(2026, 9, 30);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFissata);

            assertDoesNotThrow(() -> {
                boolean risultato = controller.validaDataImmatricolazione();
                assertTrue(risultato);
            });
        }
    }

    @Test
    void validaDataImmatricolazione_meseFuoriFinestra_lanciaDataNonValidaException() {
        LocalDate dataFissata = LocalDate.of(2026, 1, 15);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFissata);

            DataNonValidaException ex = assertThrows(DataNonValidaException.class,
                    () -> controller.validaDataImmatricolazione());

            assertTrue(ex.getMessage().contains("1° agosto al 30 settembre"));
        }
    }

    @Test
    void validaDataImmatricolazione_primoOttobre_lanciaDataNonValidaException() {
        // caso limite: il giorno subito dopo la chiusura della finestra
        LocalDate dataFissata = LocalDate.of(2026, 10, 1);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFissata);

            assertThrows(DataNonValidaException.class,
                    () -> controller.validaDataImmatricolazione());
        }
    }

    @Test
    void validaDataImmatricolazione_trentunoLuglio_lanciaDataNonValidaException() {
        // caso limite: il giorno subito prima dell'apertura della finestra
        LocalDate dataFissata = LocalDate.of(2026, 7, 31);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFissata);

            assertThrows(DataNonValidaException.class,
                    () -> controller.validaDataImmatricolazione());
        }
    }
}