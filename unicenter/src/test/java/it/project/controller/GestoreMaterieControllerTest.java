package it.project.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.project.Materia;

class GestoreMaterieControllerTest {

    private GestoreMaterieController controller;

    @BeforeEach
    void setUp() {
        controller = new GestoreMaterieController();
    }

    private Materia creaMateria(String codice) {
        Materia materia = mock(Materia.class);
        lenient().when(materia.getCodiceMateria()).thenReturn(codice);
        return materia;
    }

    // ---------------------------------------------------------------
    // trovaIdMaterieDiProfessore / associaProfessoreAMateria
    // ---------------------------------------------------------------

    @Test
    void trovaIdMaterieDiProfessore_professoreSenzaMaterie_ritornaListaVuota() {
        List<String> risultato = controller.trovaIdMaterieDiProfessore("professore-inesistente");

        assertNotNull(risultato);
        assertTrue(risultato.isEmpty());
    }

    @Test
    void associaProfessoreAMateria_associazioneSemplice_vieneRegistrata() {
        controller.associaProfessoreAMateria("1", "IS01");

        List<String> materie = controller.trovaIdMaterieDiProfessore("1");
        assertEquals(1, materie.size());
        assertTrue(materie.contains("IS01"));
    }

    @Test
    void associaProfessoreAMateria_piuMaterieAlloStessoProfessore_vengonoAccumulate() {
        controller.associaProfessoreAMateria("1", "IS01");
        controller.associaProfessoreAMateria("1", "BD01");
        controller.associaProfessoreAMateria("1", "AR01");

        List<String> materie = controller.trovaIdMaterieDiProfessore("1");
        assertEquals(3, materie.size());
        assertTrue(materie.containsAll(List.of("IS01", "BD01", "AR01")));
    }

    @Test
    void associaProfessoreAMateria_stessaCoppiaDuePpolte_eIdempotente() {
        controller.associaProfessoreAMateria("1", "IS01");
        controller.associaProfessoreAMateria("1", "IS01"); // duplicato volontario

        List<String> materie = controller.trovaIdMaterieDiProfessore("1");
        assertEquals(1, materie.size(), "L'associazione duplicata non deve creare una voce doppia");
    }

    @Test
    void associaProfessoreAMateria_stessaMateriaAPiuProfessori_funzionaPerEntrambi() {
        controller.associaProfessoreAMateria("1", "AR01");
        controller.associaProfessoreAMateria("2", "AR01");

        assertTrue(controller.trovaIdMaterieDiProfessore("1").contains("AR01"));
        assertTrue(controller.trovaIdMaterieDiProfessore("2").contains("AR01"));
    }

    // ---------------------------------------------------------------
    // isProfessoreAbilitatoAMateria
    // ---------------------------------------------------------------

    @Test
    void isProfessoreAbilitatoAMateria_associazioneEsistente_ritornaTrue() {
        controller.associaProfessoreAMateria("1", "IS01");

        assertTrue(controller.isProfessoreAbilitatoAMateria("1", "IS01"));
    }

    @Test
    void isProfessoreAbilitatoAMateria_associazioneAssente_ritornaFalse() {
        controller.associaProfessoreAMateria("1", "IS01");

        assertFalse(controller.isProfessoreAbilitatoAMateria("1", "BD01"));
    }

    @Test
    void isProfessoreAbilitatoAMateria_professoreInesistente_ritornaFalse() {
        assertFalse(controller.isProfessoreAbilitatoAMateria("professore-mai-visto", "IS01"));
    }

    // ---------------------------------------------------------------
    // addMateria / trovaMaterieDiProfessore
    // ---------------------------------------------------------------

    @Test
    void trovaMaterieDiProfessore_conMaterieRegistrateEAssociate_ritornaGliOggettiCorretti() {
        Materia is01 = creaMateria("IS01");
        Materia bd01 = creaMateria("BD01");
        controller.addMateria(is01);
        controller.addMateria(bd01);
        controller.associaProfessoreAMateria("1", "IS01");
        controller.associaProfessoreAMateria("1", "BD01");

        List<Materia> materie = controller.trovaMaterieDiProfessore("1");

        assertEquals(2, materie.size());
        assertTrue(materie.contains(is01));
        assertTrue(materie.contains(bd01));
    }

    @Test
    void trovaMaterieDiProfessore_professoreSenzaAssociazioni_ritornaListaVuota() {
        controller.addMateria(creaMateria("IS01"));

        assertTrue(controller.trovaMaterieDiProfessore("1").isEmpty());
    }

    @Test
    void trovaMaterieDiProfessore_associazionePresenteMaMateriaMaiRegistrata_vieneScartataSenzaErrori() {
        // Il professore è associato a "IS01" ma la Materia "IS01" non è mai stata
        // aggiunta con addMateria(): il metodo deve ignorarla silenziosamente
        // invece di lanciare un'eccezione (protezione contro dati inconsistenti).
        controller.associaProfessoreAMateria("1", "IS01");

        List<Materia> materie = controller.trovaMaterieDiProfessore("1");

        assertTrue(materie.isEmpty());
    }

    @Test
    void addMateria_conStessoCodiceDuePpolte_sovrascriveLaPrecedente() {
        Materia vecchia = creaMateria("IS01");
        Materia nuova = creaMateria("IS01");
        controller.addMateria(vecchia);
        controller.addMateria(nuova);
        controller.associaProfessoreAMateria("1", "IS01");

        List<Materia> materie = controller.trovaMaterieDiProfessore("1");

        assertEquals(1, materie.size());
        assertSame(nuova, materie.get(0), "La mappa interna è keyed per codice: l'ultima addMateria vince");
    }
}
