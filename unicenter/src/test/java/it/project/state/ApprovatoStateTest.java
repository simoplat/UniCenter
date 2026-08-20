package it.project.state;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.EsameSostenuto;

class ApprovatoStateTest {

    private ApprovatoState stato;
    private EsameSostenuto esameMock;

    @BeforeEach
    void setUp() {
        stato = new ApprovatoState();
        esameMock = mock(EsameSostenuto.class);
    }

    @Test
    @DisplayName("getNome() restituisce 'Approvato'")
    void testGetNome() {
        assertEquals("Approvato", stato.getNome());
    }

    @Test
    @DisplayName("accetta() su stato Approvato lancia IllegalStateException")
    void testAccettaLanciaEccezione() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stato.accetta(esameMock));
        assertTrue(ex.getMessage().contains("già stato approvato"));
        verifyNoInteractions(esameMock);
    }

    @Test
    @DisplayName("rifiuta() su stato Approvato lancia IllegalStateException")
    void testRifiutaLanciaEccezione() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stato.rifiuta(esameMock));
        assertTrue(ex.getMessage().contains("già stato approvato"));
        verifyNoInteractions(esameMock);
    }
}
