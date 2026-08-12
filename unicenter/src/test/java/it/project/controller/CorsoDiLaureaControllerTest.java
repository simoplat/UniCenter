package it.project.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.project.CorsoDiLaurea;
import it.project.Unicenter;

@ExtendWith(MockitoExtension.class)
class CorsoDiLaureaControllerTest {

    @Mock
    private Unicenter unicenter; // dipendenza non usata dai metodi correnti, ma richiesta dal costruttore

    private CorsoDiLaureaController controller;

    @BeforeEach
    void setUp() {
        controller = new CorsoDiLaureaController(unicenter);
    }

    private CorsoDiLaurea creaCorso(String nome) {
        CorsoDiLaurea corso = mock(CorsoDiLaurea.class);
        lenient().when(corso.getNome()).thenReturn(nome);
        return corso;
    }

    @Test
    void getCorsiDiLaurea_appenaCreato_ritornaListaVuota() {
        assertNotNull(controller.getCorsiDiLaurea());
        assertTrue(controller.getCorsiDiLaurea().isEmpty());
    }

    @Test
    void addCorsoDiLaurea_aggiungeIlCorsoAllaLista() {
        CorsoDiLaurea corso = creaCorso("Ingegneria Informatica");

        controller.addCorsoDiLaurea(corso);

        assertEquals(1, controller.getCorsiDiLaurea().size());
        assertSame(corso, controller.getCorsiDiLaurea().get(0));
    }

    @Test
    void addCorsoDiLaurea_permetteDiAggiungereCorsiConNomiDuplicati() {
        // Il controller non fa alcuna deduplicazione in fase di aggiunta
        controller.addCorsoDiLaurea(creaCorso("Ingegneria Informatica"));
        controller.addCorsoDiLaurea(creaCorso("Ingegneria Informatica"));

        assertEquals(2, controller.getCorsiDiLaurea().size());
    }

    @Test
    void trovaCorsoDiLaureaByNome_corsoPresente_loRitorna() {
        CorsoDiLaurea corso = creaCorso("Ingegneria Informatica");
        controller.addCorsoDiLaurea(corso);

        CorsoDiLaurea trovato = controller.trovaCorsoDiLaureaByNome("Ingegneria Informatica");

        assertSame(corso, trovato);
    }

    @Test
    void trovaCorsoDiLaureaByNome_ricercaCaseInsensitive_loRitorna() {
        CorsoDiLaurea corso = creaCorso("Ingegneria Informatica");
        controller.addCorsoDiLaurea(corso);

        assertSame(corso, controller.trovaCorsoDiLaureaByNome("INGEGNERIA INFORMATICA"));
        assertSame(corso, controller.trovaCorsoDiLaureaByNome("ingegneria informatica"));
    }

    @Test
    void trovaCorsoDiLaureaByNome_corsoAssente_ritornaNull() {
        controller.addCorsoDiLaurea(creaCorso("Ingegneria Informatica"));

        assertNull(controller.trovaCorsoDiLaureaByNome("Corso Inesistente"));
    }

    @Test
    void trovaCorsoDiLaureaByNome_listaVuota_ritornaNull() {
        assertNull(controller.trovaCorsoDiLaureaByNome("Qualsiasi Corso"));
    }

    @Test
    void trovaCorsoDiLaureaByNome_conPiuCorsi_trovaQuelloGiusto() {
        CorsoDiLaurea informatica = creaCorso("Ingegneria Informatica");
        CorsoDiLaurea meccanica = creaCorso("Ingegneria Meccanica");
        controller.addCorsoDiLaurea(informatica);
        controller.addCorsoDiLaurea(meccanica);

        assertSame(meccanica, controller.trovaCorsoDiLaureaByNome("Ingegneria Meccanica"));
    }

    @Test
    void setCorsiDiLaurea_sostituisceCompletamenteLaLista() {
        controller.addCorsoDiLaurea(creaCorso("Corso Vecchio"));

        List<CorsoDiLaurea> nuovaLista = new ArrayList<>();
        CorsoDiLaurea nuovoCorso = creaCorso("Corso Nuovo");
        nuovaLista.add(nuovoCorso);

        controller.setCorsiDiLaurea(nuovaLista);

        assertEquals(1, controller.getCorsiDiLaurea().size());
        assertSame(nuovoCorso, controller.getCorsiDiLaurea().get(0));
        assertNull(controller.trovaCorsoDiLaureaByNome("Corso Vecchio"));
    }
}
