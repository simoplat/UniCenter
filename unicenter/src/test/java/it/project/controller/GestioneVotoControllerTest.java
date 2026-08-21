package it.project.controller;

import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.exceptions.EsameNonTrovatoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Test Unitari - GestioneVotoController")
class GestioneVotoControllerTest {

    private Unicenter unicenterMock;
    private GestoreMaterieController gestoreMaterieMock;
    private GestioneVotoController controller;

    private final String MATERIA = "IS01";
    private final String PROF_ID = "P001";
    private final String MATRICOLA = "M001";

    @BeforeEach
    void setUp() {
        unicenterMock = mock(Unicenter.class);
        gestoreMaterieMock = mock(GestoreMaterieController.class);

        Materia materia = new Materia(MATERIA, "Ingegneria del Software", 9);
        when(gestoreMaterieMock.trovaMaterieByCodice(MATERIA)).thenReturn(materia);

        Professore prof = new Professore(PROF_ID, "Mario", "Rossi", "prof@test.it", "pwd", "RSSMRA80A01H501U");
        when(unicenterMock.trovaProfessore(PROF_ID)).thenReturn(Optional.of(prof));

        Studente studente = new Studente(MATRICOLA, "Luigi", "Verdi", "studente@test.it", "pwd", "VRDLGU00A01H501X", "Informatica");
        when(unicenterMock.trovaStudente(MATRICOLA)).thenReturn(Optional.of(studente));

        controller = new GestioneVotoController(unicenterMock, gestoreMaterieMock);
    }

    @Test
    @DisplayName("Pubblicazione esito con voto sufficiente")
    void testPubblicaEsito_Sufficiente() {
        EsameSostenuto esame = controller.pubblicaEsito("APP001", MATRICOLA, MATERIA, PROF_ID, 28, false, 7);
        assertNotNull(esame);
        assertEquals("In attesa di conferma", esame.getNomeStato());
        assertEquals(28, esame.getVotoNumerico());
        assertFalse(esame.isLode());
    }

    @Test
    @DisplayName("Pubblicazione esito con voto insufficiente imposta stato Bocciato")
    void testPubblicaEsito_Insufficiente() {
        EsameSostenuto esame = controller.pubblicaEsito("APP001", MATRICOLA, MATERIA, PROF_ID, 15, false, 7);
        assertNotNull(esame);
        assertEquals("Bocciato", esame.getNomeStato());
    }

    @Test
    @DisplayName("Pubblicazione esito con voto non valido lancia IllegalArgumentException")
    void testPubblicaEsito_VotoNonValido() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.pubblicaEsito("APP001", MATRICOLA, MATERIA, PROF_ID, 35, false, 7));
        assertThrows(IllegalArgumentException.class, () ->
                controller.pubblicaEsito("APP001", MATRICOLA, MATERIA, PROF_ID, -1, false, 7));
    }

    @Test
    @DisplayName("Pubblicazione esito con lode senza 30 lancia IllegalArgumentException")
    void testPubblicaEsito_LodeSenza30() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.pubblicaEsito("APP001", MATRICOLA, MATERIA, PROF_ID, 28, true, 7));
    }

    @Test
    @DisplayName("Pubblicazione esito con materia inesistente lancia IllegalArgumentException")
    void testPubblicaEsito_MateriaInesistente() {
        when(gestoreMaterieMock.trovaMaterieByCodice("INESISTENTE")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
                controller.pubblicaEsito("APP001", MATRICOLA, "INESISTENTE", PROF_ID, 28, false, 7));
    }

    @Test
    @DisplayName("Accettazione voto con successo")
    void testAccettaVoto_Successo() {
        EsameSostenuto esame = controller.pubblicaEsito("APP001", MATRICOLA, MATERIA, PROF_ID, 28, false, 7);
        boolean res = controller.accettaVoto(esame.getIdVerbale());
        assertTrue(res);
        assertEquals("Approvato", esame.getNomeStato());
    }

    @Test
    @DisplayName("Accettazione voto con esame inesistente lancia EsameNonTrovatoException")
    void testAccettaVoto_EsameInesistente() {
        assertThrows(EsameNonTrovatoException.class, () -> controller.accettaVoto("VRB-99999"));
    }

    @Test
    @DisplayName("Accettazione voto con studente non trovato lancia IllegalStateException")
    void testAccettaVoto_StudenteNonTrovato() {
        EsameSostenuto esame = controller.pubblicaEsito("APP001", "MATRICOLA_NON_ESISTE", MATERIA, PROF_ID, 28, false, 7);
        when(unicenterMock.trovaStudente("MATRICOLA_NON_ESISTE")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> controller.accettaVoto(esame.getIdVerbale()));
    }

    @Test
    @DisplayName("Rifiuto voto con successo")
    void testRifiutaVoto_Successo() {
        EsameSostenuto esame = controller.pubblicaEsito("APP001", MATRICOLA, MATERIA, PROF_ID, 28, false, 7);
        boolean res = controller.rifiutaVoto(esame.getIdVerbale());
        assertTrue(res);
        assertEquals("Rifiutato", esame.getNomeStato());
    }

    @Test
    @DisplayName("Rifiuto voto con esame inesistente lancia EsameNonTrovatoException")
    void testRifiutaVoto_EsameInesistente() {
        assertThrows(EsameNonTrovatoException.class, () -> controller.rifiutaVoto("VRB-99999"));
    }
}
