package it.project;

import it.project.exceptions.EsameNonTrovatoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Unitari - Libretto")
class LibrettoTest {

    private Libretto libretto;

    @BeforeEach
    void setUp() {
        libretto = new Libretto();
    }

    @Test
    @DisplayName("Registra e trova esame superato")
    void testRegistraETrovaEsameSuperato() {
        EsameSostenuto esame = new EsameSostenuto(
                "VRB001", "APP001", "M001", "INF01", "P001",
                28, false, 6, 7
        );
        esame.accetta();
        libretto.registraEsame(esame);

        assertTrue(libretto.isEsameSuperato("INF01"));
        assertEquals(esame, libretto.getEsameSuperato("INF01"));
    }

    @Test
    @DisplayName("getEsameSuperato su materia non presente lancia EsameNonTrovatoException")
    void testGetEsameSuperato_NonPresente_LanciaEccezione() {
        assertFalse(libretto.isEsameSuperato("MAT99"));
        assertThrows(EsameNonTrovatoException.class, () -> libretto.getEsameSuperato("MAT99"));
    }

    @Test
    @DisplayName("getEsameSuperato con codice null lancia EsameNonTrovatoException")
    void testGetEsameSuperato_CodiceNull_LanciaEccezione() {
        assertFalse(libretto.isEsameSuperato(null));
        assertThrows(EsameNonTrovatoException.class, () -> libretto.getEsameSuperato(null));
    }
}
