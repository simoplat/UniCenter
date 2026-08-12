package it.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.UtenteNonTrovatoException;

/**
 * Test per {@link Unicenter}.
 *
 * NOTA SUL DESIGN: Unicenter è un singleton (Holder pattern, costruttore
 * privato) che istanzia internamente tutti i suoi controller senza alcuna
 * dependency injection. Di conseguenza:
 *   - non è possibile passare mock ai controller interni;
 *   - lo stato (utenti, corsi, appelli) è condiviso da TUTTI i test che
 *     girano nella stessa JVM, quindi questi test non sono isolati tra loro
 *     come ci si aspetterebbe da veri unit test.
 *
 * Per rendere la suite affidabile:
 *   - il database viene popolato UNA SOLA VOLTA con @BeforeAll, tramite
 *     popolaDataBase() (unico modo pubblico per inserire un CorsoDiLaurea,
 *     dato che corsoDiLaureaController non è esposto da alcun getter);
 *   - i test che leggono dati "noti" si appoggiano ai dati seminati da
 *     popolaDataBase() (Mario Rossi, Luigi Verdi, Anna Bianchi, professori,
 *     corso "Ingegneria Informatica", materie IS01/BD01/AR01);
 *   - i test che CREANO nuovi dati usano email/codici fiscali generati con
 *     UUID per non collidere fra loro né con i dati seminati.
 *
 * Se il progetto evolve, consiglio di rifattorizzare Unicenter con
 * dependency injection dei controller per renderlo testabile in isolamento
 * e per poter azzerare lo stato tra un test e l'altro.
 */
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
    void trovaCorsoDiLaureaByNome_corsoInesistente_ritornaNull() {
        assertNull(unicenter.trovaCorsoDiLaureaByNome("Corso Che Non Esiste " + UUID.randomUUID()));
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
                        "Ingegneria Informatica", 500.0, "CF-" + UUID.randomUUID()));

        assertTrue(ex.getMessage().contains("Email già inserita"));
    }

    @Test
    void immatricolaStudente_codiceFiscaleGiaRegistrato_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unicenter.immatricolaStudente("Test", "Test", "unico-" + UUID.randomUUID() + "@studenti.it",
                        "pass123", "Ingegneria Informatica", 500.0, "CODICEFISCALEMARIOROSSI"));

        assertTrue(ex.getMessage().contains("Codice Fiscale già inserito"));
    }

    @Test
    void immatricolaStudente_emailECodiceFiscaleGiaRegistrati_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unicenter.immatricolaStudente("Test", "Test", "mario.rossi@studenti.it", "pass123",
                        "Ingegneria Informatica", 500.0, "CODICEFISCALEMARIOROSSI"));

        assertTrue(ex.getMessage().contains("Email e Codice Fiscale già inseriti"));
    }

    @Test
    void immatricolaStudente_datiUniciECorsoValido_registraLoStudente() {
        String email = "nuovo-" + UUID.randomUUID() + "@studenti.it";
        String cf = "CF-" + UUID.randomUUID();

        Studente nuovo = unicenter.immatricolaStudente("Nuovo", "Studente", email, "pass123",
                "Ingegneria Informatica", 500.0, cf);

        assertNotNull(nuovo);
        assertNotNull(nuovo.getMatricola());
        assertTrue(unicenter.esisteUtente(email));
        assertTrue(unicenter.esisteCodiceFiscale(cf));
        assertTrue(unicenter.getStudentiIscritti().contains(nuovo));
    }

    @Test
    void immatricolaStudente_corsoInesistente_lanciaIllegalArgumentException() {
        String email = "nuovo-" + UUID.randomUUID() + "@studenti.it";
        String cf = "CF-" + UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> unicenter.immatricolaStudente("Nuovo", "Studente", email, "pass123",
                        "Corso Inesistente " + UUID.randomUUID(), 500.0, cf));
    }

    // ---------------------------------------------------------------
    // validaDataImmatricolazione (delega al controller interno)
    // ---------------------------------------------------------------

    @Test
    void validaDataImmatricolazione_meseAgosto_ritornaTrue() throws DataNonValidaException {
        LocalDate dataFissata = LocalDate.of(2026, 8, 15);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFissata);

            assertTrue(unicenter.validaDataImmatricolazione());
        }
    }

    @Test
    void validaDataImmatricolazione_meseFuoriFinestra_lanciaDataNonValidaException() {
        LocalDate dataFissata = LocalDate.of(2026, 1, 15);
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(dataFissata);

            assertThrows(DataNonValidaException.class, () -> unicenter.validaDataImmatricolazione());
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
        unicenter.passwordCorretta("mario.rossi@unicenter.it", "pass123"); // associato a IS01, BD01, AR01

        List<Materia> materie = unicenter.getMaterieDelProfessore();

        assertEquals(3, materie.size());
    }
}
