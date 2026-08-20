package it.project.factory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import it.project.CorsoDiLaurea;

class CorsoDiLaureaFactoryTest {

    // =========================================================================
    // 1. CREAZIONE CON SUCCESSO
    // =========================================================================

    @ParameterizedTest(name = "Creazione corso con tipologia ''{0}'' e {1} anni attesi")
    @CsvSource({
        "Triennale, 3",
        "Magistrale, 2",
        "Magistrale a Ciclo Unico, 5",
        "Master, 2",
        "triennale, 3",
        "MAGISTRALE, 2",
        "magistrale a ciclo unico, 5",
        "master, 2"
    })
    @DisplayName("creaCorsoDiLaurea crea correttamente un corso per tutte le tipologie valide (case-insensitive)")
    void testCreaCorsoDiLaureaSuccesso(String tipologia, int anni) {
        String nomeCorso = "  Ingegneria Gestionale  ";
        CorsoDiLaurea corso = CorsoDiLaureaFactory.creaCorsoDiLaurea(nomeCorso, tipologia, anni);

        assertNotNull(corso, "Il corso non deve essere null");
        assertNotNull(corso.getId(), "L'ID corso generato non deve essere null");
        assertFalse(corso.getId().isBlank(), "L'ID corso non deve essere vuoto");
        assertEquals("Ingegneria Gestionale", corso.getNome(), "Il nome del corso deve essere trimmato");
        assertEquals(tipologia, corso.getTipologia(), "La tipologia deve coincidere");
        assertEquals(anni, corso.getAnniAccademici(), "Il numero di anni accademici deve coincidere");
        assertFalse(corso.isFinalizzato(), "Il corso appena creato non deve essere ancora finalizzato");
        assertFalse(corso.isObsoleto(), "Il corso appena creato non deve essere obsoleto");
    }

    // =========================================================================
    // 2. VALIDAZIONE NOME
    // =========================================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("creaCorsoDiLaurea lancia IllegalArgumentException per nome null, vuoto o solo spazi")
    void testNomeNonValidoLanciaEccezione(String nomeNonValido) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                CorsoDiLaureaFactory.creaCorsoDiLaurea(nomeNonValido, "Triennale", 3)
        );
        assertTrue(ex.getMessage().contains("nome del corso di laurea è obbligatorio"));
    }

    // =========================================================================
    // 3. VALIDAZIONE TIPOLOGIA
    // =========================================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Dottorato", "Corso Singolo", "Specializzazione", "Inesistente", "123"})
    @DisplayName("creaCorsoDiLaurea lancia IllegalArgumentException per tipologie non ammesse o nulle")
    void testTipologiaNonValidaLanciaEccezione(String tipologiaNonValida) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                CorsoDiLaureaFactory.creaCorsoDiLaurea("Ingegneria Informatica", tipologiaNonValida, 3)
        );
        assertTrue(ex.getMessage().contains("Tipologia non valida"));
    }

    // =========================================================================
    // 4. VALIDAZIONE COERENZA ANNI-TIPOLOGIA
    // =========================================================================

    @ParameterizedTest(name = "Tipologia ''{0}'' con {1} anni non coerenti")
    @CsvSource({
        "Triennale, 2",
        "Triennale, 4",
        "Triennale, 5",
        "Triennale, 0",
        "Triennale, -1",
        "Magistrale, 3",
        "Magistrale, 1",
        "Magistrale, 5",
        "Magistrale a Ciclo Unico, 3",
        "Magistrale a Ciclo Unico, 4",
        "Magistrale a Ciclo Unico, 6",
        "Master, 1",
        "Master, 3"
    })
    @DisplayName("creaCorsoDiLaurea lancia IllegalArgumentException se gli anni accademici non sono coerenti con la tipologia")
    void testAnniNonCoerentiLanciaEccezione(String tipologia, int anniErrati) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                CorsoDiLaureaFactory.creaCorsoDiLaurea("Ingegneria Biomedica", tipologia, anniErrati)
        );
        assertTrue(ex.getMessage().contains("non è coerente con la tipologia"));
    }

    // =========================================================================
    // 5. METODI DI UTILITY
    // =========================================================================

    @Test
    @DisplayName("getAnniPerTipologia restituisce gli anni esatti per ciascuna tipologia")
    void testGetAnniPerTipologia() {
        assertEquals(3, CorsoDiLaureaFactory.getAnniPerTipologia("Triennale"));
        assertEquals(2, CorsoDiLaureaFactory.getAnniPerTipologia("Magistrale"));
        assertEquals(5, CorsoDiLaureaFactory.getAnniPerTipologia("Magistrale a Ciclo Unico"));
        assertEquals(2, CorsoDiLaureaFactory.getAnniPerTipologia("Master"));
    }

    @Test
    @DisplayName("getAnniPerTipologia lancia IllegalArgumentException per tipologia sconosciuta")
    void testGetAnniPerTipologiaSconosciuta() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                CorsoDiLaureaFactory.getAnniPerTipologia("Dottorato")
        );
        assertTrue(ex.getMessage().contains("Tipologia sconosciuta"));
    }

    @Test
    @DisplayName("getTipologieValide restituisce l'elenco completo e un clone difensivo")
    void testGetTipologieValide() {
        String[] tipologie = CorsoDiLaureaFactory.getTipologieValide();

        assertNotNull(tipologie);
        assertEquals(4, tipologie.length);
        assertArrayEquals(new String[]{"Triennale", "Magistrale", "Magistrale a Ciclo Unico", "Master"}, tipologie);

        // Modifica difensiva: l'array restituito non deve influenzare le chiamate successive
        tipologie[0] = "MODIFICATA";
        assertEquals("Triennale", CorsoDiLaureaFactory.getTipologieValide()[0]);
    }
}
