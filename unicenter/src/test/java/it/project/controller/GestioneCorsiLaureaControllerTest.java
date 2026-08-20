package it.project.controller;

import it.project.CorsoDiLaurea;
import it.project.Materia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Unitari - GestioneCorsiLaureaController")
class GestioneCorsiLaureaControllerTest {

    private GestioneCorsiLaureaController controller;

    @BeforeEach
    void setUp() {
        controller = new GestioneCorsiLaureaController();
    }

    @Test
    @DisplayName("Creazione corso di laurea")
    void testCreaCorsoDiLaurea() {
        CorsoDiLaurea corso = controller.creaCorsoDiLaurea("Ingegneria Informatica", "Triennale", 3);
        assertNotNull(corso);
        assertEquals("Ingegneria Informatica", corso.getNome());
        assertFalse(corso.isFinalizzato());
        assertFalse(corso.isObsoleto());
    }

    @Test
    @DisplayName("Eliminazione corso di laurea non finalizzato (bozza)")
    void testEliminaCorsoDiLaurea_BozzaNonFinalizzata() {
        CorsoDiLaurea corso = controller.creaCorsoDiLaurea("Ingegneria Gestionale", "Triennale", 3);
        boolean eliminato = controller.eliminaCorsoDiLaurea(corso.getId());
        assertTrue(eliminato);
        assertNull(controller.trovaCorsoDiLaureaByCodice(corso.getId()));
    }

    @Test
    @DisplayName("Eliminazione corso di laurea obsoleto")
    void testEliminaCorsoDiLaurea_Obsoleto() {
        CorsoDiLaurea corso = controller.creaCorsoDiLaurea("Ingegneria Elettronica", "Triennale", 3);
        Materia m = new Materia("MAT01", "Analisi 1", 9);
        controller.associaMateriaACorso(corso.getId(), 1, m);
        controller.finalizzaCorso(corso.getId());
        controller.rendiObsoletoCorsoDiLaurea(corso.getId());

        boolean eliminato = controller.eliminaCorsoDiLaurea(corso.getId());
        assertTrue(eliminato);
        assertNull(controller.trovaCorsoDiLaureaByCodice(corso.getId()));
    }

    @Test
    @DisplayName("Eliminazione corso di laurea attivo e finalizzato lancia IllegalStateException")
    void testEliminaCorsoDiLaurea_AttivoFinalizzato_LanciaEccezione() {
        CorsoDiLaurea corso = controller.creaCorsoDiLaurea("Ingegneria Biomedica", "Triennale", 3);
        Materia m = new Materia("BIO01", "Biomeccanica", 6);
        controller.associaMateriaACorso(corso.getId(), 1, m);
        controller.finalizzaCorso(corso.getId());

        assertThrows(IllegalStateException.class, () -> controller.eliminaCorsoDiLaurea(corso.getId()));
    }

    @Test
    @DisplayName("Eliminazione corso di laurea inesistente lancia IllegalArgumentException")
    void testEliminaCorsoDiLaurea_Inesistente_LanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> controller.eliminaCorsoDiLaurea("NON_ESISTE"));
    }
}
