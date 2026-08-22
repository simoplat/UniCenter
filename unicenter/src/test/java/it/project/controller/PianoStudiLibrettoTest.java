package it.project.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.PianoDiStudi;
import it.project.Studente;
import it.project.Unicenter;

@DisplayName("Test - Gestione Piano di Studi Rifiutato e Visualizzazione Libretto")
class PianoStudiLibrettoTest {

    private Unicenter unicenter;
    private PianoStudiController pianoStudiController;
    private GestoreMaterieController gestoreMaterie;
    private GestioneCorsiLaureaController gestioneCorsi;
    private Studente studente;

    @BeforeEach
    void setUp() {
        unicenter = Unicenter.getInstance();

        gestoreMaterie = unicenter.getGestoreMaterie();
        gestioneCorsi = unicenter.getGestioneCorsiLaureaController();
        pianoStudiController = unicenter.getPianoStudiController();

        // Crea materie obbligatorie
        Materia m1 = new Materia("MAT01", "Analisi 1", 9);
        Materia m2 = new Materia("INF01", "Programmazione 1", 9);
        gestoreMaterie.addMateria(m1);
        gestoreMaterie.addMateria(m2);

        // Crea materie a scelta (opzionali)
        Materia mOpz1 = new Materia("OPZ01", "Machine Learning", 6);
        Materia mOpz2 = new Materia("OPZ02", "Cybersecurity", 6);
        gestoreMaterie.addMateria(mOpz1);
        gestoreMaterie.addMateria(mOpz2);

        // Crea corso di laurea (senza pre-approvare OPZ01 e OPZ02 per forzare
        // approvazione manuale)
        CorsoDiLaurea corso = new CorsoDiLaurea("CS01", "Informatica", "Triennale", 3);
        corso.aggiungiMateriaAdAnno(1, m1);
        corso.aggiungiMateriaAdAnno(1, m2);
        corso.finalizza();
        gestioneCorsi.addCorsoDiLaurea(corso);

        // Crea studente
        studente = new Studente("M123", "Mario", "Rossi", "mario@studenti.it", "pass123", "CF001", "CS01");
        studente.setTassePagate(true);
        unicenter.addUtente(studente);
    }

    @Test
    @DisplayName("Compilazione con materie non pre-approvate pone il piano in stato 'In Attesa'")
    void testPianoInAttesa() {
        boolean compilato = pianoStudiController.compilaPianoDiStudi(studente, List.of("OPZ01", "OPZ02"));
        assertTrue(compilato);
        assertEquals("In Attesa", studente.getPianoDiStudi().getNomeStato());
        assertEquals(2, studente.getPianoDiStudi().getIdMaterieAScelta().size());
        assertTrue(studente.getPianoDiStudi().getIdMaterieAScelta().contains("OPZ01"));
        assertTrue(studente.getPianoDiStudi().getIdMaterieAScelta().contains("OPZ02"));
    }

    @Test
    @DisplayName("Rifiuto del piano di studi da parte dell'amministratore rimuove le materie a scelta rifiutate")
    void testRifiutoPianoRimuoveMaterieAScelta() {
        pianoStudiController.compilaPianoDiStudi(studente, List.of("OPZ01", "OPZ02"));
        assertEquals("In Attesa", studente.getPianoDiStudi().getNomeStato());

        // L'amministratore rifiuta il piano
        boolean rifiutato = pianoStudiController.rifiutaPianoDiStudi(studente.getMatricola());
        assertTrue(rifiutato);

        assertEquals("Rifiutato", studente.getPianoDiStudi().getNomeStato());
        // Le materie a scelta rifiutate devono essere rimosse dal piano
        assertTrue(studente.getPianoDiStudi().getIdMaterieAScelta().isEmpty(),
                "Le materie a scelta rifiutate non devono essere presenti nel piano di studi.");
    }

    @Test
    @DisplayName("Rifiuto del piano mantiene eventuali materie a scelta già verbalizzate nel libretto")
    void testRifiutoPianoMantieneMaterieVerbalizzate() {
        Studente st = new Studente("M_VERB", "Mario", "Rossi", "mario.verb@studenti.it", "pass123", "CF_VERB", "CS01");
        st.setTassePagate(true);
        unicenter.addUtente(st);

        EsameSostenuto esame = new EsameSostenuto("VERB01", "APP01", "M_VERB", "OPZ01", "1", 28, false, 6, 7);
        esame.accetta();
        st.getLibretto().registraEsame(esame);

        st.getPianoDiStudi().aggiungiMateriaAScelta("OPZ01");
        pianoStudiController.compilaPianoDiStudi(st, List.of("OPZ02"));

        assertEquals("In Attesa", st.getPianoDiStudi().getNomeStato());
        assertTrue(st.getPianoDiStudi().getIdMaterieAScelta().contains("OPZ01"));
        assertTrue(st.getPianoDiStudi().getIdMaterieAScelta().contains("OPZ02"));

        pianoStudiController.rifiutaPianoDiStudi(st.getMatricola());

        assertEquals("Rifiutato", st.getPianoDiStudi().getNomeStato());
        assertEquals(1, st.getPianoDiStudi().getIdMaterieAScelta().size());
        assertTrue(st.getPianoDiStudi().getIdMaterieAScelta().contains("OPZ01"));
        assertFalse(st.getPianoDiStudi().getIdMaterieAScelta().contains("OPZ02"));
    }

    @Test
    @DisplayName("Approvazione del piano di studi pone il piano in stato 'Approvato' e mantiene le materie")
    void testApprovazionePianoMantieneMaterie() {
        pianoStudiController.compilaPianoDiStudi(studente, List.of("OPZ01", "OPZ02"));
        assertEquals("In Attesa", studente.getPianoDiStudi().getNomeStato());

        boolean approvato = pianoStudiController.approvaPianoDiStudi(studente.getMatricola());
        assertTrue(approvato);

        assertEquals("Approvato", studente.getPianoDiStudi().getNomeStato());
        assertEquals(2, studente.getPianoDiStudi().getIdMaterieAScelta().size());
    }
}
