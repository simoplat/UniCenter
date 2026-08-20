package it.project.state;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.EsameSostenuto;
import it.project.observer.ObserverEsitoVoto;

class StatoVotoIntegrationTest {

    @Test
    @DisplayName("Esame con voto >= 18 inizia in InAttesaConfermaState")
    void testStatoInizialeVotoSufficiente() {
        EsameSostenuto esame = new EsameSostenuto(
                "ESM-01", "APP-01", "M1001", "INF101", "P01", 28, false, 6, 7
        );

        assertEquals("In attesa di conferma", esame.getNomeStato());
        assertTrue(esame.getStato() instanceof InAttesaConfermaState);
        assertEquals(28, esame.getVotoNumerico());
        assertFalse(esame.isLode());
    }

    @Test
    @DisplayName("Esame con voto < 18 inizia automaticamente in BocciatoState")
    void testStatoInizialeVotoInsufficiente() {
        EsameSostenuto esame = new EsameSostenuto(
                "ESM-02", "APP-01", "M1002", "INF101", "P01", 15, false, 6, 7
        );

        assertEquals("Bocciato", esame.getNomeStato());
        assertTrue(esame.getStato() instanceof BocciatoState);
        assertEquals(15, esame.getVotoNumerico());
    }

    @Test
    @DisplayName("Transizione: InAttesaConfermaState -> accetta() -> ApprovatoState e notifica observer")
    void testTransizioneAccettaNotificaObserver() {
        EsameSostenuto esame = new EsameSostenuto(
                "ESM-03", "APP-01", "M1003", "INF101", "P01", 30, true, 9, 7
        );

        List<String> notificheStati = new ArrayList<>();
        ObserverEsitoVoto observer = (e, nuovoStato) -> notificheStati.add(nuovoStato);
        esame.aggiungiOsservatore(observer);

        esame.accetta();

        assertEquals("Approvato", esame.getNomeStato());
        assertTrue(esame.getStato() instanceof ApprovatoState);
        assertEquals(1, notificheStati.size());
        assertEquals("Approvato", notificheStati.get(0));

        // Verifiche che tentativi successivi di transizione falliscano
        assertThrows(IllegalStateException.class, esame::accetta);
        assertThrows(IllegalStateException.class, esame::rifiuta);
    }

    @Test
    @DisplayName("Transizione: InAttesaConfermaState -> rifiuta() -> RifiutatoState e notifica observer")
    void testTransizioneRifiutaNotificaObserver() {
        EsameSostenuto esame = new EsameSostenuto(
                "ESM-04", "APP-01", "M1004", "INF101", "P01", 20, false, 6, 7
        );

        List<String> notificheStati = new ArrayList<>();
        ObserverEsitoVoto observer = (e, nuovoStato) -> notificheStati.add(nuovoStato);
        esame.aggiungiOsservatore(observer);

        esame.rifiuta();

        assertEquals("Rifiutato", esame.getNomeStato());
        assertTrue(esame.getStato() instanceof RifiutatoState);
        assertEquals(1, notificheStati.size());
        assertEquals("Rifiutato", notificheStati.get(0));

        // Verifiche che tentativi successivi di transizione falliscano
        assertThrows(IllegalStateException.class, esame::accetta);
        assertThrows(IllegalStateException.class, esame::rifiuta);
    }

    @Test
    @DisplayName("Esame Bocciato non consente transizioni successive")
    void testBocciatoNonConsenteTransizioni() {
        EsameSostenuto esame = new EsameSostenuto(
                "ESM-05", "APP-01", "M1005", "INF101", "P01", 12, false, 6, 7
        );

        assertEquals("Bocciato", esame.getNomeStato());
        assertThrows(IllegalStateException.class, esame::accetta);
        assertThrows(IllegalStateException.class, esame::rifiuta);
    }
}
