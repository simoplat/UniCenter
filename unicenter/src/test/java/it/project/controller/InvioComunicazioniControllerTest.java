package it.project.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.Notifica;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;

class InvioComunicazioniControllerTest {

    private Unicenter unicenter;
    private InvioComunicazioniController controller;
    private GestoreMaterieController gestoreMaterie;
    private Professore profRossi;
    private Professore profVerdi;
    private Materia ingSoftware;
    private Materia basiDati;
    private Studente st1;
    private Studente st2;
    private Studente st3;

    @BeforeEach
    void setUp() {
        unicenter = Unicenter.getInstance();
        gestoreMaterie = unicenter.getGestoreMaterie();
        controller = unicenter.getInvioComunicazioniController();

        // Popola database iniziale per avere corsi e dati puliti
        unicenter.popolaDataBase();

        profRossi = unicenter.trovaProfessore("1").orElseThrow(); // Abilitato a IS01, BD01, AR01, PRG01, PRG02
        profVerdi = unicenter.trovaProfessore("2").orElseThrow(); // Abilitato a AR01, SO01, RET01, SIC01
        ingSoftware = gestoreMaterie.trovaMaterieByCodice("IS01");
        basiDati = gestoreMaterie.trovaMaterieByCodice("BD01");

        // Studenti di prova
        st1 = unicenter.trovaStudente("M100001").orElseThrow(); // Mario Rossi
        st2 = unicenter.trovaStudente("M100002").orElseThrow(); // Luigi Verdi
        st3 = unicenter.trovaStudente("M100003").orElseThrow(); // Anna Bianchi
    }

    @Test
    @DisplayName("UC7 - Invio comunicazione con successo a tutti gli studenti con materia nel piano di studi")
    void testInvioComunicazioneSuccesso() {
        int notificheInizialiSt1 = st1.getNotifiche().size();
        int notificheInizialiSt2 = st2.getNotifiche().size();

        // Mario Rossi (Prof) pubblica annuncio per IS01
        int destinatari = controller.inviaComunicazione(
                profRossi,
                "IS01",
                "Lezione straordinaria",
                "La lezione di lunedì si terrà in Aula Magna alle ore 10:00."
        );

        assertTrue(destinatari > 0, "Dovrebbero esserci studenti iscritti alla materia.");
        
        // Verifica ricezione notifica per lo studente st2 (Luigi Verdi) che ha IS01 e non l'ha verbalizzata
        assertTrue(st2.getPianoDiStudi().contieneMateria("IS01"));
        assertFalse(st2.getLibretto().isEsameSuperato("IS01"));
        assertEquals(notificheInizialiSt2 + 1, st2.getNotifiche().size());
        Notifica ultima = st2.getNotifiche().get(st2.getNotifiche().size() - 1);
        assertTrue(ultima.getOggetto().contains("Lezione straordinaria"));
        assertTrue(ultima.getMessaggio().contains("Aula Magna"));
    }

    @Test
    @DisplayName("UC7 - Lo studente che ha già superato e verbalizzato l'esame nel Libretto non riceve la comunicazione")
    void testStudenteConEsameNelLibrettoNonRiceveComunicazione() {
        if (!st1.getLibretto().isEsameSuperato("IS01")) {
            EsameSostenuto esameSuperato = new EsameSostenuto(
                    "ESM-TEST01", "APP-00001", st1.getMatricola(), "IS01", "1", 30, true, 9, 7
            );
            esameSuperato.accetta(); // Approvato
            st1.getLibretto().registraEsame(esameSuperato);
        }
        assertTrue(st1.getLibretto().isEsameSuperato("IS01"));

        int notifichePrima = st1.getNotifiche().size();

        // Il docente invia una comunicazione per IS01
        controller.inviaComunicazione(
                profRossi,
                "IS01",
                "Avviso Progetto",
                "Consegna del progetto software fissata per fine mese."
        );

        // st1 NON deve aver ricevuto questa notifica perché la materia è già superata
        assertEquals(notifichePrima, st1.getNotifiche().size(), "Lo studente con materia già verbalizzata non deve ricevere comunicazioni.");
    }

    @Test
    @DisplayName("UC7 - Il professore non abilitato alla materia non può inviare comunicazioni")
    void testProfessoreNonAbilitatoLanciaEccezione() {
        // Prof. Verdi ("2") non è abilitato a BD01 ("Basi di Dati", assegnata a Prof. Rossi)
        assertFalse(gestoreMaterie.isProfessoreAbilitatoAMateria("2", "BD01"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            controller.inviaComunicazione(
                    profVerdi,
                    "BD01",
                    "Cambio orario",
                    "Il ricevimento studenti è anticipato."
            );
        });

        assertTrue(ex.getMessage().contains("non è abilitato"));
    }

    @Test
    @DisplayName("UC7 - Validazione parametri nulli o vuoti")
    void testParametriNonValidiLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.inviaComunicazione(null, "IS01", "Titolo", "Messaggio"));

        assertThrows(IllegalArgumentException.class, () ->
                controller.inviaComunicazione(profRossi, "", "Titolo", "Messaggio"));

        assertThrows(IllegalArgumentException.class, () ->
                controller.inviaComunicazione(profRossi, "IS01", "   ", "Messaggio"));

        assertThrows(IllegalArgumentException.class, () ->
                controller.inviaComunicazione(profRossi, "IS01", "Titolo", ""));

        assertThrows(IllegalArgumentException.class, () ->
                controller.inviaComunicazione(profRossi, "MATERIA_INESISTENTE", "Titolo", "Messaggio"));
    }

    @Test
    @DisplayName("UC7 - Il docente riceve una notifica di conferma dell'invio")
    void testDocenteRiceveNotificaDiConferma() {
        int notifProfPrima = profRossi.getNotifiche().size();

        controller.inviaComunicazione(
                profRossi,
                "PRG01",
                "Materiale didattico disponibile",
                "I lucidi delle lezioni 1-5 sono disponibili su UniCenter."
        );

        assertEquals(notifProfPrima + 1, profRossi.getNotifiche().size());
        Notifica conferma = profRossi.getNotifiche().get(profRossi.getNotifiche().size() - 1);
        assertTrue(conferma.getOggetto().contains("Conferma Invio"));
    }
}
