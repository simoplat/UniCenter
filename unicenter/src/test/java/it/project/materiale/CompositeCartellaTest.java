package it.project.materiale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.materiale.repository.MaterialeDidatticoRepository;
import it.project.materiale.repository.FileSystemMaterialeDidatticoRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Unitari - Pattern Composite (Cartella ed ElementoDidattico)")
public class CompositeCartellaTest {

    private Cartella cartellaRadice;
    private MaterialeDidatticoRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("unicenter_test_composite");
        repo = new FileSystemMaterialeDidatticoRepository(tempDir);
        cartellaRadice = new Cartella("IS01_Ingegneria_del_Software", "Radice", "IS01", null, "IS01");
    }

    @Test
    @DisplayName("Aggiunta e navigazione ricorsiva del Composite")
    void testAggiuntaENavigazioneComposite() {
        Cartella cartellaProf = cartellaRadice.creaSubCartella("Prof_Mario_Rossi", "Cartella Docente", "1");
        Cartella cartellaSlide = cartellaProf.creaSubCartella("Slide", "Slide del corso", "1");

        byte[] fakePdf = new byte[1024]; // 1 KB
        MaterialeDidattico slide1 = cartellaSlide.creaMateriale(
                "Lezione1.pdf", "Slide Lezione 1", TipoMateriale.SLIDE, fakePdf, "1", repo);

        byte[] fakeTxt = new byte[512]; // 512 B
        MaterialeDidattico traccia = cartellaProf.creaMateriale(
                "Traccia.txt", "Traccia laboratorio", TipoMateriale.TESTO, fakeTxt, "1", repo);

        // Verifica gerarchia e conteggio
        assertEquals(1, cartellaRadice.elenca().size());
        assertEquals(2, cartellaProf.elenca().size());
        assertEquals(1, cartellaSlide.elenca().size());

        // Verifica calcolo ricorsivo dimensione Composite
        assertEquals(1024 + 512, cartellaRadice.getDimensioneBytes());
        assertEquals(1024 + 512, cartellaProf.getDimensioneBytes());
        assertEquals(1024, cartellaSlide.getDimensioneBytes());

        // Verifica ricerca ricorsiva
        assertNotNull(cartellaRadice.trovaElemento(slide1.getId()));
        assertNotNull(cartellaRadice.trovaElemento(traccia.getId()));
        assertNotNull(cartellaRadice.trovaElemento(cartellaSlide.getId()));
    }

    @Test
    @DisplayName("Rimozione ricorsiva elemento dal Composite")
    void testRimozioneElementoComposite() {
        Cartella cartellaProf = cartellaRadice.creaSubCartella("Prof_Mario_Rossi", "Docente", "1");
        MaterialeDidattico mat = cartellaProf.creaMateriale(
                "Appunti.txt", "Note", TipoMateriale.TESTO, new byte[256], "1", repo);

        assertEquals(256, cartellaRadice.getDimensioneBytes());
        assertTrue(cartellaRadice.rimuoviElemento(mat.getId()));
        assertEquals(0, cartellaRadice.getDimensioneBytes());
        assertNull(cartellaRadice.trovaElemento(mat.getId()));
    }

    @Test
    @DisplayName("Anteprima polimorfica di una cartella Composite")
    void testVisualizzaCartella() {
        Cartella sub = cartellaRadice.creaSubCartella("Esercizi", "Note", "1");
        sub.creaMateriale("file1.txt", "", TipoMateriale.TESTO, new byte[100], "1", repo);

        AnteprimaRisultato ant = cartellaRadice.visualizza();
        assertNotNull(ant);
        assertEquals("application/x-directory", ant.getMimeType());
        assertTrue(ant.getContenutoTestuale().contains("Cartella contenente"));
    }
}
