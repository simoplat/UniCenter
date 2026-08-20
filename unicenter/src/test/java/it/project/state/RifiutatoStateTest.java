package it.project.state;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.EsameSostenuto;

class RifiutatoStateTest {

    private RifiutatoState stato;
    private EsameSostenuto esameMock;

    @BeforeEach
    void setUp() {
        stato = new RifiutatoState();
        esameMock = mock(EsameSostenuto.class);
    }

    @Test
    @DisplayName("getNome() restituisce 'Rifiutato'")
    void testGetNome() {
        assertEquals("Rifiutato", stato.getNome());
    }

    @Test
    @DisplayName("accetta() su stato Rifiutato lancia IllegalStateException")
    void testAccettaLanciaEccezione() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stato.accetta(esameMock));
        assertTrue(ex.getMessage().contains("già stato rifiutato"));
        verifyNoInteractions(esameMock);
    }

    @Test
    @DisplayName("rifiuta() su stato Rifiutato lancia IllegalStateException")
    void testRifiutaLanciaEccezione() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stato.rifiuta(esameMock));
        assertTrue(ex.getMessage().contains("già stato rifiutato"));
        verifyNoInteractions(esameMock);
    }
}
