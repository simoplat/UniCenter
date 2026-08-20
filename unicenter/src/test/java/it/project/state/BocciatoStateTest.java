package it.project.state;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.EsameSostenuto;

class BocciatoStateTest {

    private BocciatoState stato;
    private EsameSostenuto esameMock;

    @BeforeEach
    void setUp() {
        stato = new BocciatoState();
        esameMock = mock(EsameSostenuto.class);
    }

    @Test
    @DisplayName("getNome() restituisce 'Bocciato'")
    void testGetNome() {
        assertEquals("Bocciato", stato.getNome());
    }

    @Test
    @DisplayName("accetta() su stato Bocciato lancia IllegalStateException")
    void testAccettaLanciaEccezione() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stato.accetta(esameMock));
        assertTrue(ex.getMessage().contains("bocciato"));
        verifyNoInteractions(esameMock);
    }

    @Test
    @DisplayName("rifiuta() su stato Bocciato lancia IllegalStateException")
    void testRifiutaLanciaEccezione() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stato.rifiuta(esameMock));
        assertTrue(ex.getMessage().contains("bocciato"));
        verifyNoInteractions(esameMock);
    }
}
