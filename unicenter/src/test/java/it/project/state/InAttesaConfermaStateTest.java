package it.project.state;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import it.project.EsameSostenuto;

class InAttesaConfermaStateTest {

    private InAttesaConfermaState stato;
    private EsameSostenuto esameMock;

    @BeforeEach
    void setUp() {
        stato = new InAttesaConfermaState();
        esameMock = mock(EsameSostenuto.class);
    }

    @Test
    @DisplayName("getNome() restituisce 'In attesa di conferma'")
    void testGetNome() {
        assertEquals("In attesa di conferma", stato.getNome());
    }

    @Test
    @DisplayName("accetta() transita l'esame allo stato ApprovatoState")
    void testAccettaTransitaAdApprovato() {
        stato.accetta(esameMock);

        ArgumentCaptor<IStatoVoto> captor = ArgumentCaptor.forClass(IStatoVoto.class);
        verify(esameMock, times(1)).setStato(captor.capture());

        IStatoVoto nuovoStato = captor.getValue();
        assertNotNull(nuovoStato);
        assertTrue(nuovoStato instanceof ApprovatoState);
        assertEquals("Approvato", nuovoStato.getNome());
    }

    @Test
    @DisplayName("rifiuta() transita l'esame allo stato RifiutatoState")
    void testRifiutaTransitaARifiutato() {
        stato.rifiuta(esameMock);

        ArgumentCaptor<IStatoVoto> captor = ArgumentCaptor.forClass(IStatoVoto.class);
        verify(esameMock, times(1)).setStato(captor.capture());

        IStatoVoto nuovoStato = captor.getValue();
        assertNotNull(nuovoStato);
        assertTrue(nuovoStato instanceof RifiutatoState);
        assertEquals("Rifiutato", nuovoStato.getNome());
    }
}
