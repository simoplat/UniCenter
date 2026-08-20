package it.project.controller;

import it.project.*;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.PostiNonValidi;
import it.project.validation.IscrizioneValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestioneAppelliControllerTest {

    private Unicenter unicenterMock;
    private GestioneAppelliController controller;

    private static final String MATERIA = "INF101";

    @BeforeEach
    void setUp() {
        unicenterMock = mock(Unicenter.class);
        // Di default nessun utente autenticato: bypassa il controllo
        // "professore abilitato alla materia" per i test che non lo
        // riguardano direttamente.
        when(unicenterMock.getCurrentUser()).thenReturn(null);
        controller = new GestioneAppelliController(unicenterMock);
    }

    // ---------------------------------------------------------------
    // Helper di reflection
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Appello> getAppelliInterni() throws Exception {
        Field f = GestioneAppelliController.class.getDeclaredField("appelli");
        f.setAccessible(true);
        return (List<Appello>) f.get(controller);
    }

    private void aggiungiAppelloDirettamente(Appello appello) throws Exception {
        getAppelliInterni().add(appello);
    }

    private void iniettaValidatorChainMock(IscrizioneValidator mockValidator) throws Exception {
        Field f = GestioneAppelliController.class.getDeclaredField("validatorChain");
        f.setAccessible(true);
        f.set(controller, mockValidator);
    }

    private Studente creaStudente(String matricola) {
        return new Studente(matricola, "Mario", "Rossi", "mario.rossi@test.it",
                "password", "RSSMRA00A01H501U", "Informatica");
    }

    private Appello creaAppelloValido(String codice) {
        return new Appello(codice, MATERIA, LocalDateTime.now().plusDays(10),
                "Aula 1", 30, "A-Z", LocalDate.now().plusDays(5));
    }

    // =================================================================
    // creaNuovoAppello / validateAppello
    // =================================================================

    @Test
    void creaNuovoAppello_conParametriValidi_restituisceTrueECreaAppello() throws Exception {
        boolean risultato = controller.creaNuovoAppello(MATERIA,
                LocalDateTime.now().plusDays(3), "Aula 2", 20, "A-L",
                LocalDate.now().plusDays(1));

        assertTrue(risultato);
        List<Appello> trovati = controller.trovaAppelliByIdMateria(List.of(MATERIA));
        assertEquals(1, trovati.size());
        Appello creato = trovati.get(0);
        assertNotNull(creato.getCodiceAppello());
        assertFalse(creato.getCodiceAppello().isEmpty());
        assertEquals("A-L", creato.getVincoloLetteraCognome());
    }

    @Test
    void creaNuovoAppello_conDataPassata_lanciaDataNonValidaException() {
        assertThrows(DataNonValidaException.class,
                () -> controller.creaNuovoAppello(MATERIA, LocalDateTime.now().minusDays(1),
                        "Aula 1", 10, "A-Z", LocalDate.now().plusDays(1)));
    }

    @Test
    void creaNuovoAppello_conDataNulla_lanciaDataNonValidaException() {
        assertThrows(DataNonValidaException.class, () -> controller.creaNuovoAppello(MATERIA, null,
                "Aula 1", 10, "A-Z", LocalDate.now().plusDays(1)));
    }

    @Test
    void creaNuovoAppello_conTermineIscrizioneDopoDataAppello_lanciaEccezione() {
        LocalDateTime dataAppello = LocalDateTime.now().plusDays(2);
        assertThrows(DataNonValidaException.class, () -> controller.creaNuovoAppello(MATERIA, dataAppello,
                "Aula 1", 10, "A-Z", dataAppello.toLocalDate().plusDays(1)));
    }

    @Test
    void creaNuovoAppello_conTermineIscrizioneNelPassato_lanciaEccezione() {
        assertThrows(DataNonValidaException.class,
                () -> controller.creaNuovoAppello(MATERIA, LocalDateTime.now().plusDays(5),
                        "Aula 1", 10, "A-Z", LocalDate.now().minusDays(1)));
    }

    @Test
    void creaNuovoAppello_conPostiNonPositivi_lanciaPostiNonValidi() {
        assertThrows(PostiNonValidi.class, () -> controller.creaNuovoAppello(MATERIA, LocalDateTime.now().plusDays(2),
                "Aula 1", 0, "A-Z", LocalDate.now().plusDays(1)));
    }

    @Test
    void creaNuovoAppello_professoreNonAbilitatoAllaMateria_lanciaIllegalArgumentException() {
        Professore prof = new Professore("P001", "Luigi", "Bianchi",
                "luigi.bianchi@test.it", "pwd", "BNCLGU00A01H501U");
        when(unicenterMock.getCurrentUser()).thenReturn(prof);
        when(unicenterMock.isProfessoreAbilitatoAMateria(MATERIA)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> controller.creaNuovoAppello(MATERIA, LocalDateTime.now().plusDays(2),
                        "Aula 1", 10, "A-Z", LocalDate.now().plusDays(1)));
    }

    @Test
    void creaNuovoAppello_professoreAbilitato_creaCorrettamente() throws Exception {
        Professore prof = new Professore("P001", "Luigi", "Bianchi",
                "luigi.bianchi@test.it", "pwd", "BNCLGU00A01H501U");
        when(unicenterMock.getCurrentUser()).thenReturn(prof);
        when(unicenterMock.isProfessoreAbilitatoAMateria(MATERIA)).thenReturn(true);

        boolean risultato = controller.creaNuovoAppello(MATERIA,
                LocalDateTime.now().plusDays(2), "Aula 1", 10, "A-Z",
                LocalDate.now().plusDays(1));

        assertTrue(risultato);
    }

    // ---------------------------------------------------------------
    // Normalizzazione vincolo lettera cognome (testata indirettamente
    // tramite creaNuovoAppello, dato che il metodo è privato)
    // ---------------------------------------------------------------

    @Test
    void creaNuovoAppello_vincoloInvertito_vieneRiordinatoAlfabeticamente() throws Exception {
        controller.creaNuovoAppello(MATERIA, LocalDateTime.now().plusDays(2),
                "Aula 1", 10, "Z-A", LocalDate.now().plusDays(1));

        Appello creato = controller.trovaAppelliByIdMateria(List.of(MATERIA)).get(0);
        assertEquals("A-Z", creato.getVincoloLetteraCognome());
    }

    @Test
    void creaNuovoAppello_vincoloConAccentiESpazi_vieneNormalizzato() throws Exception {
        controller.creaNuovoAppello(MATERIA, LocalDateTime.now().plusDays(2),
                "Aula 1", 10, " é - z ", LocalDate.now().plusDays(1));

        Appello creato = controller.trovaAppelliByIdMateria(List.of(MATERIA)).get(0);
        assertEquals("E-Z", creato.getVincoloLetteraCognome());
    }

    @Test
    void creaNuovoAppello_vincoloVuoto_vieneImpostatoComeStringaVuota() throws Exception {
        controller.creaNuovoAppello(MATERIA, LocalDateTime.now().plusDays(2),
                "Aula 1", 10, "   ", LocalDate.now().plusDays(1));

        Appello creato = controller.trovaAppelliByIdMateria(List.of(MATERIA)).get(0);
        assertEquals("", creato.getVincoloLetteraCognome());
    }

    @Test
    void creaNuovoAppello_vincoloFormatoNonValido_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.creaNuovoAppello(MATERIA, LocalDateTime.now().plusDays(2),
                        "Aula 1", 10, "ABC", LocalDate.now().plusDays(1)));
    }

    // =================================================================
    // iscriviStudente
    // =================================================================

    @Test
    void iscriviStudente_conValidazioneSuperata_restituisceTrueEInviaNotifica() throws Exception {
        Appello appello = creaAppelloValido("APP001");
        aggiungiAppelloDirettamente(appello);

        // Non stubbiamo validate(): un mock non configurato non lancia
        // eccezioni (si limita a restituire il valore di default), quindi
        // la validazione "passa" implicitamente. Non usiamo doNothing()
        // perché validate() potrebbe non essere void (es. ritorna
        // boolean/void a seconda dell'implementazione reale non nota).
        IscrizioneValidator validatorMock = mock(IscrizioneValidator.class);
        iniettaValidatorChainMock(validatorMock);

        Studente studente = creaStudente("M001");
        boolean risultato = controller.iscriviStudente(studente, "APP001");

        assertTrue(risultato);
        assertTrue(appello.getIscritti().contains(studente));
        assertEquals(1, studente.getNotifiche().size());
        verify(validatorMock, times(1)).validate(studente, appello);
    }

    @Test
    void iscriviStudente_appelloInesistente_lanciaIllegalArgumentException() {
        Studente studente = creaStudente("M002");
        assertThrows(IllegalArgumentException.class, () -> controller.iscriviStudente(studente, "NON_ESISTE"));
    }

    @Test
    void iscriviStudente_studenteGiaIscritto_lanciaIllegalStateException() throws Exception {
        Appello appello = creaAppelloValido("APP002");
        Studente studente = creaStudente("M003");
        appello.aggiungiIscritto(studente);
        aggiungiAppelloDirettamente(appello);

        assertThrows(IllegalStateException.class, () -> controller.iscriviStudente(studente, "APP002"));
    }

    @Test
    void iscriviStudente_validazioneFallita_lanciaEccezioneENonIscrive() throws Exception {
        Appello appello = creaAppelloValido("APP003");
        aggiungiAppelloDirettamente(appello);

        IscrizioneValidator validatorMock = mock(IscrizioneValidator.class);
        doThrow(new IllegalStateException("Vincolo non rispettato"))
                .when(validatorMock).validate(any(), any());
        iniettaValidatorChainMock(validatorMock);

        Studente studente = creaStudente("M004");
        assertThrows(IllegalStateException.class, () -> controller.iscriviStudente(studente, "APP003"));
        assertFalse(appello.getIscritti().contains(studente));
        assertTrue(studente.getNotifiche().isEmpty());
    }

    @Test
    void iscriviStudente_esameGiaSuperatoNelLibretto_lanciaIllegalStateException() throws Exception {
        Appello appello = creaAppelloValido("APP_SUP");
        aggiungiAppelloDirettamente(appello);

        Studente studente = creaStudente("M020");
        EsameSostenuto esameSuperato = new EsameSostenuto("ESM-001", "APP_OLD", "M020", MATERIA, "P001", 28, false, 9, 7);
        esameSuperato.accetta();
        studente.getLibretto().registraEsame(esameSuperato);

        assertThrows(IllegalStateException.class, () -> controller.iscriviStudente(studente, "APP_SUP"));
        assertFalse(appello.getIscritti().contains(studente));
    }

    // =================================================================
    // disiscriviStudente
    // =================================================================

    @Test
    void disiscriviStudente_studenteIscritto_restituisceTrueERimuove() throws Exception {
        Appello appello = creaAppelloValido("APP004");
        Studente studente = creaStudente("M005");
        appello.aggiungiIscritto(studente);
        aggiungiAppelloDirettamente(appello);

        boolean risultato = controller.disiscriviStudente(studente, "APP004");

        assertTrue(risultato);
        assertFalse(appello.getIscritti().contains(studente));
        assertEquals(1, studente.getNotifiche().size());
    }

    @Test
    void disiscriviStudente_studenteNonIscritto_lanciaIllegalStateException() throws Exception {
        Appello appello = creaAppelloValido("APP005");
        aggiungiAppelloDirettamente(appello);
        Studente studente = creaStudente("M006");

        assertThrows(IllegalStateException.class, () -> controller.disiscriviStudente(studente, "APP005"));
    }

    @Test
    void disiscriviStudente_appelloInesistente_lanciaIllegalArgumentException() {
        Studente studente = creaStudente("M007");
        assertThrows(IllegalArgumentException.class, () -> controller.disiscriviStudente(studente, "NON_ESISTE"));
    }

    // =================================================================
    // Ricerche
    // =================================================================

    @Test
    void trovaAppelliByIdMateria_conMaterieCorrispondenti_restituisceAppelliCorretti() throws Exception {
        Appello a1 = creaAppelloValido("APP006");
        Appello a2 = new Appello("APP007", "MAT999", LocalDateTime.now().plusDays(1),
                "Aula 3", 5, "", LocalDate.now().plusDays(1));
        aggiungiAppelloDirettamente(a1);
        aggiungiAppelloDirettamente(a2);

        List<Appello> risultato = controller.trovaAppelliByIdMateria(List.of(MATERIA));

        assertEquals(1, risultato.size());
        assertEquals("APP006", risultato.get(0).getCodiceAppello());
    }

    @Test
    void trovaAppelliByIdMateria_conListaVuota_restituisceListaVuota() {
        assertTrue(controller.trovaAppelliByIdMateria(List.of()).isEmpty());
    }

    @Test
    void trovaAppelliByIdMateria_conListaNull_restituisceListaVuota() {
        assertTrue(controller.trovaAppelliByIdMateria(null).isEmpty());
    }

    @Test
    void trovaAppelliByIdMateria_senzaAppelliRegistrati_restituisceListaVuota() {
        assertTrue(controller.trovaAppelliByIdMateria(List.of(MATERIA)).isEmpty());
    }

    @Test
    void trovaAppelliPrenotabiliByStudente_escludeAppelliGiaPrenotati() throws Exception {
        Appello a1 = creaAppelloValido("APP008");
        Appello a2 = new Appello("APP009", MATERIA, LocalDateTime.now().plusDays(2),
                "Aula 4", 15, "", LocalDate.now().plusDays(1));
        Studente studente = creaStudente("M008");
        a1.aggiungiIscritto(studente);
        aggiungiAppelloDirettamente(a1);
        aggiungiAppelloDirettamente(a2);

        List<Appello> prenotabili = controller.trovaAppelliPrenotabiliByStudente(studente, List.of(MATERIA));

        assertEquals(1, prenotabili.size());
        assertEquals("APP009", prenotabili.get(0).getCodiceAppello());
    }

    @Test
    void trovaAppelloByIdAppello_esistente_restituisceAppello() throws Exception {
        Appello appello = creaAppelloValido("APP010");
        aggiungiAppelloDirettamente(appello);

        assertEquals(appello, controller.trovaAppelloByIdAppello("APP010"));
    }

    @Test
    void trovaAppelloByIdAppello_inesistente_restituisceNull() {
        assertNull(controller.trovaAppelloByIdAppello("SCONOSCIUTO"));
    }

    @Test
    void trovaIscrittiByIdAppello_restituisceListaIscritti() throws Exception {
        Appello appello = creaAppelloValido("APP011");
        Studente studente = creaStudente("M009");
        appello.aggiungiIscritto(studente);
        aggiungiAppelloDirettamente(appello);

        List<Studente> iscritti = controller.trovaIscrittiByIdAppello("APP011");

        assertEquals(1, iscritti.size());
        assertTrue(iscritti.contains(studente));
    }

    @Test
    void trovaIscrittiByIdAppello_appelloInesistente_restituisceListaVuota() {
        assertTrue(controller.trovaIscrittiByIdAppello("SCONOSCIUTO").isEmpty());
    }

    @Test
    void generaCodiceAppello_restituisceCodiceNonVuoto() {
        String codice = controller.generaCodiceAppello();
        assertNotNull(codice);
        assertFalse(codice.isEmpty());
    }

    @Test
    void appelliPrenotatiByStudente_restituisceSoloAppelliDelloStudente() throws Exception {
        Appello a1 = creaAppelloValido("APP012");
        Appello a2 = new Appello("APP013", MATERIA, LocalDateTime.now().plusDays(3),
                "Aula 5", 8, "", LocalDate.now().plusDays(1));
        Studente studente = creaStudente("M010");
        a1.aggiungiIscritto(studente);
        aggiungiAppelloDirettamente(a1);
        aggiungiAppelloDirettamente(a2);

        List<Appello> risultato = controller.appelliPrenotatiByStudente(studente);

        assertEquals(1, risultato.size());
        assertEquals("APP012", risultato.get(0).getCodiceAppello());
    }

    @Test
    void appelliPrenotatiByStudente_conStudenteNull_restituisceListaVuota() {
        assertTrue(controller.appelliPrenotatiByStudente(null).isEmpty());
    }

    // =================================================================
    // modificaAppello
    // =================================================================

    @Test
    void modificaAppello_conDatiValidi_aggiornaCampiEInviaNotifica() throws Exception {
        Appello appello = creaAppelloValido("APP014");
        Studente studente = creaStudente("M011");
        appello.aggiungiIscritto(studente);
        aggiungiAppelloDirettamente(appello);

        LocalDateTime nuovaData = LocalDateTime.now().plusDays(20);
        boolean risultato = controller.modificaAppello("APP014", nuovaData, "Aula 9",
                50, "M-Z", LocalDate.now().plusDays(15));

        assertTrue(risultato);
        assertEquals(nuovaData, appello.getDataOra());
        assertEquals("Aula 9", appello.getAula());
        assertEquals(49, appello.getPostiDisponibili());
        assertEquals("M-Z", appello.getVincoloLetteraCognome());
        assertEquals(1, studente.getNotifiche().size());
    }

    @Test
    void modificaAppello_appelloInesistente_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> controller.modificaAppello("SCONOSCIUTO",
                LocalDateTime.now().plusDays(1), "Aula 1", 10, "A-Z",
                LocalDate.now().plusDays(1)));
    }

    @Test
    void modificaAppello_postiInferioriAgliIscritti_lanciaPostiNonValidi() throws Exception {
        Appello appello = creaAppelloValido("APP015");
        appello.aggiungiIscritto(creaStudente("M012"));
        appello.aggiungiIscritto(creaStudente("M013"));
        aggiungiAppelloDirettamente(appello);

        assertThrows(PostiNonValidi.class, () -> controller.modificaAppello("APP015", LocalDateTime.now().plusDays(5),
                "Aula 1", 1, "A-Z", LocalDate.now().plusDays(1)));
    }

    // =================================================================
    // eliminaAppello
    // =================================================================

    @Test
    void eliminaAppello_esistente_restituisceTrueELoRimuove() throws Exception {
        Appello appello = creaAppelloValido("APP016");
        Studente studente = creaStudente("M014");
        appello.aggiungiIscritto(studente);
        aggiungiAppelloDirettamente(appello);

        boolean risultato = controller.eliminaAppello("APP016");

        assertTrue(risultato);
        assertNull(controller.trovaAppelloByIdAppello("APP016"));
        assertEquals(1, studente.getNotifiche().size());
    }

    @Test
    void eliminaAppello_inesistente_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> controller.eliminaAppello("SCONOSCIUTO"));
    }
}