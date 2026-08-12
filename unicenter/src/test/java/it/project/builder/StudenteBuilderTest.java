package it.project.builder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import it.project.Studente;

class StudenteBuilderTest {

    // ---------------------------------------------------------------
    // build() con dati validi
    // ---------------------------------------------------------------

    @Test
    void build_conTuttiICampiValidi_creaStudenteCorretto() {
        Studente studente = new StudenteBuilder()
                .setNome("Mario")
                .setCognome("Rossi")
                .setEmail("Mario.Rossi@Studenti.It") // volutamente con maiuscole/spazi da normalizzare
                .setPassword("pass123")
                .setCorsoDiLaurea("Ingegneria Informatica")
                .setCodiceFiscale("CODICEFISCALEMARIOROSSI")
                .build();

        assertEquals("Mario", studente.getNome());
        assertEquals("Rossi", studente.getCognome());
        assertEquals("mario.rossi@studenti.it", studente.getEmail(), "L'email deve essere normalizzata in minuscolo");
        assertEquals("Ingegneria Informatica", studente.getCorsoDiLaurea());
        assertEquals("CODICEFISCALEMARIOROSSI", studente.getCodiceFiscale());
        assertNotNull(studente.getMatricola());
        assertFalse(studente.getMatricola().isBlank());
    }

    @Test
    void build_conCampiConSpaziEsterni_liRimuove() {
        Studente studente = new StudenteBuilder()
                .setNome("  Mario  ")
                .setCognome("  Rossi  ")
                .setEmail("  mario.rossi@studenti.it  ")
                .setPassword("pass123")
                .setCorsoDiLaurea("  Ingegneria Informatica  ")
                .setCodiceFiscale("CODICEFISCALEMARIOROSSI")
                .build();

        assertEquals("Mario", studente.getNome());
        assertEquals("Rossi", studente.getCognome());
        assertEquals("mario.rossi@studenti.it", studente.getEmail());
        assertEquals("Ingegneria Informatica", studente.getCorsoDiLaurea());
    }

    @Test
    void build_chiamateInOrdineDiverso_funzionaComunque() {
        // Il builder non impone un ordine di chiamata dei setter
        Studente studente = new StudenteBuilder()
                .setCodiceFiscale("CF001")
                .setCorsoDiLaurea("Ingegneria Informatica")
                .setPassword("pass123")
                .setEmail("test@studenti.it")
                .setCognome("Verdi")
                .setNome("Luigi")
                .build();

        assertEquals("Luigi", studente.getNome());
        assertEquals("Verdi", studente.getCognome());
    }

    @Test
    void build_dueVolte_generaMatricoleDiverse() {
        Studente studente1 = new StudenteBuilder()
                .setNome("Mario").setCognome("Rossi").setEmail("mario@studenti.it")
                .setPassword("pass123").setCorsoDiLaurea("Ingegneria Informatica")
                .setCodiceFiscale("CF001")
                .build();

        Studente studente2 = new StudenteBuilder()
                .setNome("Luigi").setCognome("Verdi").setEmail("luigi@studenti.it")
                .setPassword("pass123").setCorsoDiLaurea("Ingegneria Informatica")
                .setCodiceFiscale("CF002")
                .build();

        assertNotEquals(studente1.getMatricola(), studente2.getMatricola(),
                "MatricolaGenerator deve produrre matricole diverse per studenti diversi");
    }

    // ---------------------------------------------------------------
    // setNome
    // ---------------------------------------------------------------

    @Test
    void setNome_null_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new StudenteBuilder().setNome(null));
        assertEquals("Il nome non può essere vuoto.", ex.getMessage());
    }

    @Test
    void setNome_stringaVuotaOSoloSpazi_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setNome(""));
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setNome("   "));
    }

    @Test
    void setNome_valido_ritornaLoStessoBuilderPerChaining() {
        StudenteBuilder builder = new StudenteBuilder();
        assertSame(builder, builder.setNome("Mario"));
    }

    // ---------------------------------------------------------------
    // setCognome
    // ---------------------------------------------------------------

    @Test
    void setCognome_null_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setCognome(null));
    }

    @Test
    void setCognome_vuoto_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setCognome("   "));
    }

    // ---------------------------------------------------------------
    // setEmail
    // ---------------------------------------------------------------

    @Test
    void setEmail_null_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new StudenteBuilder().setEmail(null));
        assertEquals("L'email è obbligatoria e non può essere vuota.", ex.getMessage());
    }

    @Test
    void setEmail_vuota_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setEmail("   "));
    }

    @Test
    void setEmail_formatoNonValido_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new StudenteBuilder().setEmail("email-senza-chiocciola"));
        assertTrue(ex.getMessage().contains("Formato email non valido"));
    }

    @Test
    void setEmail_senzaDominio_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setEmail("mario@"));
    }

    @Test
    void setEmail_conMaiuscole_vieneNormalizzataInMinuscolo() {
        StudenteBuilder builder = new StudenteBuilder().setEmail("Mario.Rossi@STUDENTI.IT");
        Studente studente = builder
                .setNome("Mario").setCognome("Rossi").setPassword("pass123")
                .setCorsoDiLaurea("Ingegneria Informatica").setCodiceFiscale("CF001")
                .build();

        assertEquals("mario.rossi@studenti.it", studente.getEmail());
    }

    // ---------------------------------------------------------------
    // setPassword
    // ---------------------------------------------------------------

    @Test
    void setPassword_null_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setPassword(null));
    }

    @Test
    void setPassword_menoDiQuattroCaratteri_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new StudenteBuilder().setPassword("123"));
        assertEquals("La password deve contenere almeno 4 caratteri.", ex.getMessage());
    }

    @Test
    void setPassword_esattamenteQuattroCaratteri_nonLanciaEccezione() {
        assertDoesNotThrow(() -> new StudenteBuilder().setPassword("1234"));
    }

    // ---------------------------------------------------------------
    // setCorsoDiLaurea
    // ---------------------------------------------------------------

    @Test
    void setCorsoDiLaurea_null_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setCorsoDiLaurea(null));
    }

    @Test
    void setCorsoDiLaurea_vuoto_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setCorsoDiLaurea("   "));
    }

    // ---------------------------------------------------------------
    // setCodiceFiscale
    // ---------------------------------------------------------------

    @Test
    void setCodiceFiscale_null_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setCodiceFiscale(null));
    }

    @Test
    void setCodiceFiscale_vuoto_lanciaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StudenteBuilder().setCodiceFiscale("   "));
    }

    @Test
    void setCodiceFiscale_valido_nonVieneTrimmato() {
        // A differenza degli altri setter, setCodiceFiscale non chiama trim() sul valore salvato:
        // documentiamo questo comportamento com'è nel codice attuale.
        Studente studente = new StudenteBuilder()
                .setNome("Mario").setCognome("Rossi").setEmail("mario@studenti.it")
                .setPassword("pass123").setCorsoDiLaurea("Ingegneria Informatica")
                .setCodiceFiscale("  CF001  ")
                .build();

        assertEquals("  CF001  ", studente.getCodiceFiscale());
    }

    // ---------------------------------------------------------------
    // build() con campi mancanti (comportamento attuale: non valida al momento del build)
    // ---------------------------------------------------------------

    @Test
    void build_senzaAverImpostatoAlcunCampo_nonLanciaEccezioneMaProduceCampiNulli() {
        // NOTA: build() non esegue alcuna validazione di completezza; se un setter
        // non viene mai chiamato, il campo corrispondente resta null nello Studente
        // creato. Segnaliamo questo comportamento con un test esplicito, perché
        // potrebbe non essere quello desiderato (mancanza di controllo di completezza).
        Studente studente = new StudenteBuilder().build();

        assertNull(studente.getNome());
        assertNull(studente.getCognome());
        assertNull(studente.getEmail());
        assertNull(studente.getCorsoDiLaurea());
        assertNull(studente.getCodiceFiscale());
        assertNotNull(studente.getMatricola(), "La matricola viene generata comunque da MatricolaGenerator");
    }
}
