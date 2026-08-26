package it.project.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.CorsoDiLaurea;
import it.project.Studente;
import it.project.Unicenter;
import it.project.database.ClockProvider;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.exceptions.DataNonValidaException;
import it.project.strategy.ICalcoloTasseStrategy;

/**
 * Test unitari per {@link ImmatricolazioneController}.
 *
 * Dipendenze Maven necessarie (oltre a junit-jupiter):
 * - org.mockito:mockito-core:5.x (include l'inline mock maker, necessario per
 * mockStatic)
 * - org.mockito:mockito-junit-jupiter:5.x
 */
class ImmatricolazioneControllerTest {

    private Unicenter unicenter;
    private ImmatricolazioneController controller;

    private static final String NOME = "Mario";
    private static final String COGNOME = "Rossi";
    private static final String EMAIL = "mario.rossi.nuovo@studenti.it";
    private static final String PASSWORD = "pass123";
    private static final String CORSO = "Ingegneria Informatica";
    private static final String CODICE_FISCALE = "CODICEFISCALEMARIOROSSI";

    @BeforeEach
    void setUp() {
        unicenter = Unicenter.getInstance();
        unicenter.popolaDataBase();
        controller = new ImmatricolazioneController(unicenter);
    }

    // ---------------------------------------------------------------
    // immatricolaStudente
    // ---------------------------------------------------------------

    @Test
    void immatricolaStudente_corsoTrovato_creaStudenteConDatiCorretti() {
        Studente studente = controller.immatricolaStudente(
                NOME, COGNOME, EMAIL, PASSWORD, CORSO, CODICE_FISCALE);

        assertNotNull(studente);
        assertEquals(NOME, studente.getNome());
        assertEquals(COGNOME, studente.getCognome());
        assertEquals(EMAIL, studente.getEmail());
        assertEquals(CODICE_FISCALE, studente.getCodiceFiscale());
        assertEquals("ING-INF", studente.getIdCorsoDiLaurea());
        assertNotNull(studente.getMatricola());
        assertFalse(studente.isTassePagate(), "Alla creazione le tasse non devono risultare pagate");
    }

    @Test
    void immatricolaStudente_corsoNonTrovato_lanciaCorsoDiLaureaNonTrovatoException() {
        assertThrows(CorsoDiLaureaNonTrovatoException.class,
                () -> controller.immatricolaStudente(
                        NOME, COGNOME, EMAIL, PASSWORD, "Corso Inesistente", CODICE_FISCALE));
    }

    @Test
    void immatricolaStudente_nomeNonValido_propagaEccezioneDalBuilder() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.immatricolaStudente(
                        "   ", COGNOME, EMAIL, PASSWORD, CORSO, CODICE_FISCALE));

        assertEquals("Il nome non può essere vuoto e deve contenere solo lettere.", ex.getMessage());
    }

    @Test
    void immatricolaStudente_emailNonValida_propagaEccezioneDalBuilder() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.immatricolaStudente(
                        NOME, COGNOME, "email-non-valida", PASSWORD, CORSO, CODICE_FISCALE));
    }

    @Test
    void immatricolaStudente_passwordTroppoCorta_propagaEccezioneDalBuilder() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.immatricolaStudente(
                        NOME, COGNOME, EMAIL, "123", CORSO, CODICE_FISCALE));
    }

    @Test
    void immatricolaStudente_usaStrategyPerCalcolareTotaleTasse_conMockIniettataViaReflection() throws Exception {
        ICalcoloTasseStrategy strategyMock = mock(ICalcoloTasseStrategy.class);
        when(strategyMock.calcolaTasse(ImmatricolazioneController.TASSA_IMMATRICOLAZIONE, false))
                .thenReturn(1234.56);

        Field strategyField = ImmatricolazioneController.class.getDeclaredField("calcoloTasseStrategy");
        strategyField.setAccessible(true);
        strategyField.set(controller, strategyMock);

        Studente studente = controller.immatricolaStudente(
                NOME, COGNOME, EMAIL, PASSWORD, CORSO, CODICE_FISCALE);

        assertEquals(1234.56, studente.getTasse(), 0.0001);
        verify(strategyMock, times(1))
                .calcolaTasse(ImmatricolazioneController.TASSA_IMMATRICOLAZIONE, false);
    }

    // ---------------------------------------------------------------
    // validaDataImmatricolazione
    // ---------------------------------------------------------------

    @AfterEach
    void tearDown() {
        ClockProvider.resetClock();
    }

    @Test
    void validaDataImmatricolazione_meseAgosto_ritornaTrue() {
        ClockProvider.setFixedDate(LocalDate.of(2026, 8, 1));
        assertDoesNotThrow(() -> {
            boolean risultato = controller.validaDataImmatricolazione();
            assertTrue(risultato);
        });
    }

    @Test
    void validaDataImmatricolazione_meseSettembre_ritornaTrue() {
        ClockProvider.setFixedDate(LocalDate.of(2026, 9, 30));
        assertDoesNotThrow(() -> {
            boolean risultato = controller.validaDataImmatricolazione();
            assertTrue(risultato);
        });
    }

    @Test
    void validaDataImmatricolazione_meseFuoriFinestra_lanciaDataNonValidaException() {
        ClockProvider.setFixedDate(LocalDate.of(2026, 1, 15));
        DataNonValidaException ex = assertThrows(DataNonValidaException.class,
                () -> controller.validaDataImmatricolazione());
        assertTrue(ex.getMessage().contains("1° agosto al 30 settembre"));
    }

    @Test
    void validaDataImmatricolazione_primoOttobre_lanciaDataNonValidaException() {
        ClockProvider.setFixedDate(LocalDate.of(2026, 10, 1));
        assertThrows(DataNonValidaException.class,
                () -> controller.validaDataImmatricolazione());
    }

    @Test
    void validaDataImmatricolazione_trentunoLuglio_lanciaDataNonValidaException() {
        ClockProvider.setFixedDate(LocalDate.of(2026, 7, 31));
        assertThrows(DataNonValidaException.class,
                () -> controller.validaDataImmatricolazione());
    }

    // ---------------------------------------------------------------
    // validaDataRinnovoIscrizione
    // ---------------------------------------------------------------

    @Test
    void validaDataRinnovoIscrizione_finestraAperta_ritornaTrue() {
        // Settembre
        ClockProvider.setFixedDate(LocalDate.of(2026, 9, 15));
        assertTrue(controller.isFinestraRinnovoAperta());
        assertDoesNotThrow(() -> controller.validaDataRinnovoIscrizione());

        // Dicembre
        ClockProvider.setFixedDate(LocalDate.of(2026, 12, 31));
        assertTrue(controller.isFinestraRinnovoAperta());
        assertDoesNotThrow(() -> controller.validaDataRinnovoIscrizione());
    }

    @Test
    void validaDataRinnovoIscrizione_finestraChiusa_lanciaDataNonValidaException() {
        // Luglio (prima)
        ClockProvider.setFixedDate(LocalDate.of(2026, 7, 15));
        assertFalse(controller.isFinestraRinnovoAperta());
        assertThrows(DataNonValidaException.class, () -> controller.validaDataRinnovoIscrizione());

        // Gennaio (dopo)
        ClockProvider.setFixedDate(LocalDate.of(2027, 1, 10));
        assertFalse(controller.isFinestraRinnovoAperta());
        assertThrows(DataNonValidaException.class, () -> controller.validaDataRinnovoIscrizione());
    }

    @Test
    void validaDataRinnovoIscrizione_studenteStessoAnnoImmatricolazione_lanciaDataNonValidaException() {
        ClockProvider.setFixedDate(LocalDate.of(2026, 10, 1));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);

        assertFalse(controller.isFinestraRinnovoAperta(studente));
        DataNonValidaException ex = assertThrows(DataNonValidaException.class,
                () -> controller.validaDataRinnovoIscrizione(studente));
        assertTrue(ex.getMessage().contains("stesso anno solare di immatricolazione"));
    }

    @Test
    void validaDataRinnovoIscrizione_studenteAnnoSuccessivo_ritornaTrue() {
        ClockProvider.setFixedDate(LocalDate.of(2027, 10, 1));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);

        assertTrue(controller.isFinestraRinnovoAperta(studente));
        assertDoesNotThrow(() -> controller.validaDataRinnovoIscrizione(studente));
    }

    // ---------------------------------------------------------------
    // rinnovaIscrizioneStudente
    // ---------------------------------------------------------------

    @Test
    void rinnovaIscrizioneStudente_studenteNull_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> controller.rinnovaIscrizioneStudente(null));
    }

    @Test
    void rinnovaIscrizioneStudente_stessoAnnoImmatricolazione_lanciaDataNonValidaException() {
        ClockProvider.setFixedDate(LocalDate.of(2026, 10, 1));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.setTassePagate(true);

        assertThrows(DataNonValidaException.class, () -> controller.rinnovaIscrizioneStudente(studente));
    }

    @Test
    void rinnovaIscrizioneStudente_tasseNonPagate_lanciaIllegalStateException() {
        ClockProvider.setFixedDate(LocalDate.of(2027, 10, 1));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.setTassePagate(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> controller.rinnovaIscrizioneStudente(studente));
        assertTrue(ex.getMessage().contains("tasse universitarie pendenti"));
    }

    @Test
    void rinnovaIscrizioneStudente_giaRinnovato_lanciaIllegalStateException() {
        ClockProvider.setFixedDate(LocalDate.of(2027, 10, 1));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.setTassePagate(true);
        studente.setRinnovoEffettuatoPerAnnoCorrente(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> controller.rinnovaIscrizioneStudente(studente));
        assertTrue(ex.getMessage().contains("già effettuato"));
    }

    @Test
    void rinnovaIscrizioneStudente_successo_inCorso() throws Exception {
        ClockProvider.setFixedDate(LocalDate.of(2027, 10, 1));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.setTassePagate(true);
        assertEquals(1, studente.getAnnoCorrente());
        assertFalse(studente.isFuoriCorso());

        boolean ok = controller.rinnovaIscrizioneStudente(studente);
        assertTrue(ok);
        assertEquals(2, studente.getAnnoCorrente());
        assertFalse(studente.isFuoriCorso(), "Al 2° anno di una triennale è ancora In Corso");
        assertFalse(studente.isTassePagate(), "Dopo il rinnovo le nuove tasse devono risultare non ancora pagate");
        assertEquals(ImmatricolazioneController.TASSA_RINNOVO_BASE, studente.getTasse());
        assertTrue(studente.isRinnovoEffettuatoPerAnnoCorrente());
        assertFalse(studente.getNotifiche().isEmpty(), "Deve essere stata creata una notifica di rinnovo");
    }

    @Test
    void rinnovaIscrizioneStudente_successo_diventaFuoriCorso() throws Exception {
        ClockProvider.setFixedDate(LocalDate.of(2029, 10, 1));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.getCarriera().setAnnoCorrente(3);
        studente.setTassePagate(true);

        boolean ok = controller.rinnovaIscrizioneStudente(studente);
        assertTrue(ok);
        assertEquals(4, studente.getAnnoCorrente());
        assertTrue(studente.isFuoriCorso(), "Al 4° anno di una triennale diventa Fuori Corso");
        assertEquals(ImmatricolazioneController.TASSA_RINNOVO_BASE + 300.0, studente.getTasse(),
                "Per studenti fuori corso deve applicarsi la maggiorazione di 300 EUR");
    }

    @Test
    void rinnovaIscrizioneStudente_rinnoviMultiAnnoConsecutivi_successo() throws Exception {
        // Immatricolazione nel 2026
        ClockProvider.setFixedDate(LocalDate.of(2026, 9, 10));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.setTassePagate(true);
        assertEquals(1, studente.getAnnoCorrente());

        // 1° Rinnovo: Ottobre 2027 -> 2° Anno (In Corso)
        ClockProvider.setFixedDate(LocalDate.of(2027, 10, 15));
        assertTrue(controller.isFinestraRinnovoAperta(studente));
        assertTrue(controller.rinnovaIscrizioneStudente(studente));
        assertEquals(2, studente.getAnnoCorrente());
        assertFalse(studente.isFuoriCorso());
        assertEquals(2027, studente.getAnnoUltimoRinnovo());

        // Pagamento tasse anno 2
        studente.setTassePagate(true);

        // 2° Rinnovo: Ottobre 2028 -> 3° Anno (In Corso)
        ClockProvider.setFixedDate(LocalDate.of(2028, 10, 15));
        assertTrue(controller.isFinestraRinnovoAperta(studente));
        assertTrue(controller.rinnovaIscrizioneStudente(studente));
        assertEquals(3, studente.getAnnoCorrente());
        assertFalse(studente.isFuoriCorso());
        assertEquals(2028, studente.getAnnoUltimoRinnovo());

        // Pagamento tasse anno 3
        studente.setTassePagate(true);

        // 3° Rinnovo: Ottobre 2029 -> 4° Anno (Fuori Corso con maggiorazione)
        ClockProvider.setFixedDate(LocalDate.of(2029, 10, 15));
        assertTrue(controller.isFinestraRinnovoAperta(studente));
        assertTrue(controller.rinnovaIscrizioneStudente(studente));
        assertEquals(4, studente.getAnnoCorrente());
        assertTrue(studente.isFuoriCorso());
        assertEquals(2029, studente.getAnnoUltimoRinnovo());
        assertEquals(ImmatricolazioneController.TASSA_RINNOVO_BASE + 300.0, studente.getTasse());
    }

    // ---------------------------------------------------------------
    // getStatoRinnovoStudente
    // ---------------------------------------------------------------

    @Test
    void getStatoRinnovoStudente_studenteNull_restituisceMappaVuota() {
        var stato = controller.getStatoRinnovoStudente(null);
        assertTrue(stato.isEmpty());
    }

    @Test
    void getStatoRinnovoStudente_studenteIdoneo_restituisceDatiCorretti() {
        ClockProvider.setFixedDate(LocalDate.of(2027, 10, 15));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.setTassePagate(true);

        var stato = controller.getStatoRinnovoStudente(studente);
        assertFalse(stato.isEmpty());
        assertEquals(true, stato.get("idoneo"));
        assertEquals(true, stato.get("finestraAperta"));
        assertEquals(true, stato.get("tassePregressePagate"));
        assertEquals(false, stato.get("giaRinnovato"));
        assertEquals(1, stato.get("annoAttuale"));
        assertEquals(2, stato.get("prossimoAnno"));
        assertNull(stato.get("motivoBlocco"));
    }

    @Test
    void getStatoRinnovoStudente_tasseNonPagate_motivoBloccoValorizzato() {
        ClockProvider.setFixedDate(LocalDate.of(2027, 10, 15));
        Studente studente = new Studente("M001", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "ING-INF");
        studente.setAnnoImmatricolazione(2026);
        studente.setTassePagate(false);

        var stato = controller.getStatoRinnovoStudente(studente);
        assertEquals(false, stato.get("idoneo"));
        assertNotNull(stato.get("motivoBlocco"));
        assertTrue(((String) stato.get("motivoBlocco")).contains("tasse universitarie"));
    }
}