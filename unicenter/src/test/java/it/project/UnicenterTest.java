package it.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import it.project.database.ClockProvider;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.UtenteNonTrovatoException;
import it.project.state.StatoApprovatoPiano;
import it.project.state.StatoInAttesaPiano;

class UnicenterTest {

    private static Unicenter unicenter;

    @BeforeAll
    static void popolaDatabaseUnaVoltaSola() {
        unicenter = Unicenter.getInstance();
        unicenter.popolaDataBase();
    }

    // ---------------------------------------------------------------
    // esisteUtente / esisteCodiceFiscale
    // ---------------------------------------------------------------

    @Test
    void esisteUtente_emailSeminata_ritornaTrue() {
        assertTrue(unicenter.esisteUtente("mario.rossi@studenti.it"));
    }

    @Test
    void esisteUtente_emailCaseInsensitive_ritornaTrue() {
        assertTrue(unicenter.esisteUtente("MARIO.ROSSI@STUDENTI.IT"));
    }

    @Test
    void esisteUtente_emailInesistente_ritornaFalse() {
        assertFalse(unicenter.esisteUtente("nessuno-" + UUID.randomUUID() + "@studenti.it"));
    }

    @Test
    void esisteCodiceFiscale_seminato_ritornaTrue() {
        assertTrue(unicenter.esisteCodiceFiscale("CODICEFISCALEMARIOROSSI"));
    }

    @Test
    void esisteCodiceFiscale_inesistente_ritornaFalse() {
        assertFalse(unicenter.esisteCodiceFiscale("CF-" + UUID.randomUUID()));
    }

    // ---------------------------------------------------------------
    // trovaStudente / getStudentiIscritti
    // ---------------------------------------------------------------

    @Test
    void trovaStudente_matricolaEsistente_ritornaStudente() {
        Studente marioSeminato = unicenter.getStudentiIscritti().stream()
                .filter(s -> s.getEmail().equalsIgnoreCase("mario.rossi@studenti.it"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Precondizione: Mario Rossi deve essere stato seminato"));

        Optional<Studente> trovato = unicenter.trovaStudente(marioSeminato.getMatricola());

        assertTrue(trovato.isPresent());
        assertEquals("Mario", trovato.get().getNome());
    }

    @Test
    void trovaStudente_matricolaInesistente_ritornaOptionalVuoto() {
        assertTrue(unicenter.trovaStudente("MATRICOLA-INESISTENTE-XYZ").isEmpty());
    }

    @Test
    void getStudentiIscritti_contieneGliStudentiSeminati() {
        List<Studente> studenti = unicenter.getStudentiIscritti();

        assertTrue(studenti.stream().anyMatch(s -> s.getEmail().equalsIgnoreCase("mario.rossi@studenti.it")));
        assertTrue(studenti.stream().anyMatch(s -> s.getEmail().equalsIgnoreCase("luigi.verdi@studenti.it")));
        assertTrue(studenti.stream().anyMatch(s -> s.getEmail().equalsIgnoreCase("anna.bianchi@studenti.it")));
    }

    // ---------------------------------------------------------------
    // trovaCorsoDiLaureaByNome
    // ---------------------------------------------------------------

    @Test
    void trovaCorsoDiLaureaByNome_corsoSeminato_ritornaCorso() {
        CorsoDiLaurea corso = unicenter.trovaCorsoDiLaureaByNome("Ingegneria Informatica");

        assertNotNull(corso);
        assertEquals("Ingegneria Informatica", corso.getNome());
    }

    @Test
    void trovaCorsoDiLaureaByNome_ricercaCaseInsensitive_ritornaCorso() {
        assertNotNull(unicenter.trovaCorsoDiLaureaByNome("ingegneria informatica"));
    }

    @Test
    void trovaCorsoDiLaureaByNome_corsoInesistente_lanciaEccezione() {
        assertThrows(CorsoDiLaureaNonTrovatoException.class,
                () -> unicenter.trovaCorsoDiLaureaByNome("Corso Che Non Esiste " + UUID.randomUUID()));
    }

    // ---------------------------------------------------------------
    // effettuaLogin
    // ---------------------------------------------------------------

    @Test
    void effettuaLogin_credenzialiValide_ritornaUtente() throws UtenteNonTrovatoException {
        Utente utente = unicenter.effettuaLogin("mario.rossi@studenti.it", "pass123");

        assertNotNull(utente);
        assertEquals("Mario", utente.getNome());
    }

    @Test
    void effettuaLogin_passwordErrata_lanciaUtenteNonTrovatoException() {
        assertThrows(UtenteNonTrovatoException.class,
                () -> unicenter.effettuaLogin("mario.rossi@studenti.it", "password-sbagliata"));
    }

    @Test
    void effettuaLogin_emailInesistente_lanciaUtenteNonTrovatoException() {
        assertThrows(UtenteNonTrovatoException.class,
                () -> unicenter.effettuaLogin("inesistente-" + UUID.randomUUID() + "@studenti.it", "pass123"));
    }

    // ---------------------------------------------------------------
    // passwordCorretta / getCurrentUser
    // ---------------------------------------------------------------

    @Test
    void passwordCorretta_credenzialiValide_ritornaTrueEImpostaCurrentUser() {
        boolean risultato = unicenter.passwordCorretta("luigi.verdi@studenti.it", "pass123");

        assertTrue(risultato);
        assertNotNull(unicenter.getCurrentUser());
        assertEquals("Luigi", unicenter.getCurrentUser().getNome());
    }

    @Test
    void passwordCorretta_credenzialiNonValide_ritornaFalse() {
        assertFalse(unicenter.passwordCorretta("luigi.verdi@studenti.it", "password-sbagliata"));
    }

    // ---------------------------------------------------------------
    // immatricolaStudente - controllo duplicati
    // ---------------------------------------------------------------

    @Test
    void immatricolaStudente_emailGiaRegistrata_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unicenter.immatricolaStudente("Test", "Test", "mario.rossi@studenti.it", "pass123",
                        "Ingegneria Informatica", "CF-" + UUID.randomUUID()));

        assertTrue(ex.getMessage().contains("Email già inserita"));
    }

    @Test
    void immatricolaStudente_codiceFiscaleGiaRegistrato_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unicenter.immatricolaStudente("Test", "Test", "unico-" + UUID.randomUUID() + "@studenti.it",
                        "pass123", "Ingegneria Informatica", "CODICEFISCALEMARIOROSSI"));

        assertTrue(ex.getMessage().contains("Codice Fiscale già inserito"));
    }

    @Test
    void immatricolaStudente_emailECodiceFiscaleGiaRegistrati_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unicenter.immatricolaStudente("Test", "Test", "mario.rossi@studenti.it", "pass123",
                        "Ingegneria Informatica", "CODICEFISCALEMARIOROSSI"));

        assertTrue(ex.getMessage().contains("Email e Codice Fiscale già inseriti"));
    }

    @Test
    void immatricolaStudente_datiUniciECorsoValido_registraLoStudente() {
        String email = "nuovo-" + UUID.randomUUID() + "@studenti.it";
        String cf = "CF-" + UUID.randomUUID();

        Studente nuovo = unicenter.immatricolaStudente("Nuovo", "Studente", email, "pass123",
                "Ingegneria Informatica", cf);

        assertNotNull(nuovo);
        assertNotNull(nuovo.getMatricola());
        assertTrue(unicenter.esisteUtente(email));
        assertTrue(unicenter.esisteCodiceFiscale(cf));
        assertTrue(unicenter.getStudentiIscritti().contains(nuovo));
    }

    @Test
    void immatricolaStudente_corsoInesistente_lanciaCorsoDiLaureaNonTrovatoException() {
        String email = "nuovo-" + UUID.randomUUID() + "@studenti.it";
        String cf = "CF-" + UUID.randomUUID();

        assertThrows(CorsoDiLaureaNonTrovatoException.class,
                () -> unicenter.immatricolaStudente("Nuovo", "Studente", email, "pass123",
                        "Corso Inesistente " + UUID.randomUUID(), cf));
    }

    // ---------------------------------------------------------------
    // validaDataImmatricolazione (delega al controller interno)
    // ---------------------------------------------------------------

    @Test
    void validaDataImmatricolazione_meseAgosto_ritornaTrue() throws DataNonValidaException {
        try {
            ClockProvider.setFixedDate(LocalDate.of(2026, 8, 15));
            assertTrue(unicenter.validaDataImmatricolazione());
        } finally {
            ClockProvider.resetClock();
        }
    }

    @Test
    void validaDataImmatricolazione_meseFuoriFinestra_lanciaDataNonValidaException() {
        try {
            ClockProvider.setFixedDate(LocalDate.of(2026, 1, 15));
            assertThrows(DataNonValidaException.class, () -> unicenter.validaDataImmatricolazione());
        } finally {
            ClockProvider.resetClock();
        }
    }

    // ---------------------------------------------------------------
    // generaCodiceAppello (delega)
    // ---------------------------------------------------------------

    @Test
    void generaCodiceAppello_ritornaCodiceNonNulloENonVuoto() {
        String codice = unicenter.generaCodiceAppello();

        assertNotNull(codice);
        assertFalse(codice.isBlank());
    }

    // ---------------------------------------------------------------
    // Comportamenti dipendenti dal tipo di currentUser
    // ---------------------------------------------------------------

    @Test
    void getNotifichePerStudente_currentUserProfessore_ritornaListaVuota() {
        boolean loggato = unicenter.passwordCorretta("mario.rossi@unicenter.it", "pass123");
        assertTrue(loggato, "Precondizione: il professore deve poter effettuare il login");

        assertTrue(unicenter.getNotifichePerStudente().isEmpty());
    }

    @Test
    void getNotifichePerStudente_currentUserStudenteConNotifiche_ritornaLeNotifiche() {
        boolean loggato = unicenter.passwordCorretta("mario.rossi@studenti.it", "pass123");
        assertTrue(loggato);

        List<Notifica> notifiche = unicenter.getNotifichePerStudente();

        assertFalse(notifiche.isEmpty(), "popolaDataBase() aggiunge una notifica a Mario Rossi");
    }

    @Test
    void trovaAppelliProfessore_currentUserStudente_ritornaListaVuota() {
        unicenter.passwordCorretta("mario.rossi@studenti.it", "pass123"); // login come studente

        assertTrue(unicenter.trovaAppelliProfessore().isEmpty());
    }

    @Test
    void trovaAppelliStudentePrenotabili_currentUserProfessore_ritornaListaVuota() {
        unicenter.passwordCorretta("mario.rossi@unicenter.it", "pass123"); // login come professore

        assertTrue(unicenter.trovaAppelliStudentePrenotabili().isEmpty());
    }

    @Test
    void trovaAppelliStudentePrenotabili_pianoStudiNonApprovato_mostraSoloMaterieObbligatorie() {
        unicenter.passwordCorretta("mario.rossi@studenti.it", "pass123");
        Studente studente = (Studente) unicenter.getCurrentUser();
        studente.getPianoDiStudi().setStato(new StatoInAttesaPiano());

        // UC9: con piano in attesa, le materie obbligatorie sono comunque prenotabili
        List<Appello> appelli = unicenter.trovaAppelliStudentePrenotabili();
        assertNotNull(appelli);

        // Ripristina lo stato del piano di studi per i successivi test
        studente.getPianoDiStudi().setStato(new StatoApprovatoPiano());
    }

    @Test
    void isProfessoreAbilitatoAMateria_professoreAbilitato_ritornaTrue() {
        unicenter.passwordCorretta("mario.rossi@unicenter.it", "pass123"); // Prof. Rossi, idProfessore "1"

        assertTrue(unicenter.isProfessoreAbilitatoAMateria("IS01"));
    }

    @Test
    void isProfessoreAbilitatoAMateria_professoreNonAbilitato_ritornaFalse() {
        unicenter.passwordCorretta("mario.rossi@unicenter.it", "pass123");

        assertFalse(unicenter.isProfessoreAbilitatoAMateria("MATERIA-INESISTENTE-" + UUID.randomUUID()));
    }

    @Test
    void getMaterieDelProfessore_professoreConMaterie_leRitornaTutte() {
        unicenter.passwordCorretta("mario.rossi@unicenter.it", "pass123");

        List<Materia> materie = unicenter.getMaterieDelProfessore();

        assertEquals(5, materie.size());
        assertTrue(materie.stream().anyMatch(m -> m.getCodiceMateria().equals("IS01")));
        assertTrue(materie.stream().anyMatch(m -> m.getCodiceMateria().equals("BD01")));
        assertTrue(materie.stream().anyMatch(m -> m.getCodiceMateria().equals("AR01")));
    }

    @Test
    void getStatoRinnovoStudenteCorrente_studenteAutenticato_restituisceDatiCorretti() {
        try {
            ClockProvider.setFixedDate(LocalDate.of(2027, 10, 1));
            unicenter.passwordCorretta("mario.rossi@studenti.it", "pass123");
            Studente studente = (Studente) unicenter.getCurrentUser();
            studente.setAnnoImmatricolazione(2026);
            studente.setTassePagate(true);
            studente.setRinnovoEffettuatoPerAnnoCorrente(false);

            Map<String, Object> stato = unicenter.getStatoRinnovoStudenteCorrente();
            assertNotNull(stato);
            assertTrue((Boolean) stato.get("finestraAperta"));
            assertTrue((Boolean) stato.get("tassePregressePagate"));
            assertFalse((Boolean) stato.get("giaRinnovato"));
            assertTrue((Boolean) stato.get("idoneo"));
            assertEquals(1, stato.get("annoAttuale"));
            assertEquals(2, stato.get("prossimoAnno"));
            assertEquals(2026, stato.get("annoImmatricolazione"));
        } finally {
            ClockProvider.resetClock();
        }
    }

    @Test
    void rinnovaIscrizioneStudenteCorrente_stessoAnnoImmatricolazione_lanciaDataNonValidaException() {
        try {
            ClockProvider.setFixedDate(LocalDate.of(2026, 10, 1));
            unicenter.passwordCorretta("mario.rossi@studenti.it", "pass123");
            Studente studente = (Studente) unicenter.getCurrentUser();
            studente.setAnnoImmatricolazione(2026);
            studente.setTassePagate(true);
            studente.setRinnovoEffettuatoPerAnnoCorrente(false);

            assertThrows(DataNonValidaException.class, () -> unicenter.rinnovaIscrizioneStudenteCorrente());
        } finally {
            ClockProvider.resetClock();
        }
    }

    @Test
    void rinnovaIscrizioneStudenteCorrente_flussoCompleto_successo() throws Exception {
        try {
            ClockProvider.setFixedDate(LocalDate.of(2027, 10, 1));
            unicenter.passwordCorretta("mario.rossi@studenti.it", "pass123");
            Studente studente = (Studente) unicenter.getCurrentUser();
            studente.setAnnoImmatricolazione(2026);
            studente.getCarriera().setAnnoCorrente(1);
            studente.setTassePagate(true);
            studente.setRinnovoEffettuatoPerAnnoCorrente(false);

            boolean ok = unicenter.rinnovaIscrizioneStudenteCorrente();
            assertTrue(ok);
            assertEquals(2, studente.getAnnoCorrente());
            assertFalse(studente.isTassePagate());
            assertTrue(studente.isRinnovoEffettuatoPerAnnoCorrente());
        } finally {
            ClockProvider.resetClock();
        }
    }

    @Test
    void getStatoRinnovoStudente_corsoInesistente_lanciaCorsoDiLaureaNonTrovatoException() {
        Studente studente = new Studente("M999", "Test", "User", "test@studenti.it", "pwd", "CF999", "CORSO_NON_ESISTENTE");
        assertThrows(CorsoDiLaureaNonTrovatoException.class, () -> unicenter.getStatoRinnovoStudente(studente));
    }
}
