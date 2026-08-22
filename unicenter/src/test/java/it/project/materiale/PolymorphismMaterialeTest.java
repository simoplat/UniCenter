package it.project.materiale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.materiale.repository.FileSystemMaterialeDidatticoRepository;
import it.project.materiale.repository.MaterialeDidatticoRepository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Unitari - Pattern Polymorphism (Gerarchia MaterialeDidattico)")
public class PolymorphismMaterialeTest {

    private MaterialeDidatticoRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("unicenter_test_poly");
        repo = new FileSystemMaterialeDidatticoRepository(tempDir);
    }

    @Test
    @DisplayName("Polimorfismo DocumentoPdf: anteprima, download e mime-type")
    void testDocumentoPdfPolimorfismo() throws Exception {
        byte[] pdfContent = "%PDF-1.4 Fake PDF Content".getBytes(StandardCharsets.UTF_8);
        repo.salvaFile("IS01/test.pdf", pdfContent);

        MaterialeDidattico pdf = new DocumentoPdf(
                "test.pdf", "Descrizione PDF", "IS01/test.pdf",
                pdfContent.length, "1", "IS01", repo);

        assertEquals("application/pdf", pdf.getMimeType());
        assertArrayEquals(pdfContent, pdf.scarica());

        AnteprimaRisultato ant = pdf.anteprima();
        assertEquals("application/pdf", ant.getMimeType());
        assertEquals("test.pdf", ant.getNome());
        assertNotNull(ant.getDownloadUrl());
    }

    @Test
    @DisplayName("Polimorfismo FileTesto: lettura diretta del contenuto e download")
    void testFileTestoPolimorfismo() throws Exception {
        String testoReale = "Line 1: Esempio di codice\nLine 2: Testo di prova";
        byte[] txtBytes = testoReale.getBytes(StandardCharsets.UTF_8);
        repo.salvaFile("IS01/note.txt", txtBytes);

        MaterialeDidattico txt = new FileTesto(
                "note.txt", "Appunti", "IS01/note.txt",
                txtBytes.length, "1", "IS01", repo);

        assertEquals("text/plain", txt.getMimeType());
        assertArrayEquals(txtBytes, txt.scarica());

        AnteprimaRisultato ant = txt.anteprima();
        assertEquals("text/plain", ant.getMimeType());
        assertEquals(testoReale, ant.getContenutoTestuale());
    }

    @Test
    @DisplayName("Polimorfismo Slide e Dispensa")
    void testSlideEDispensaPolimorfismo() throws Exception {
        byte[] slideBytes = "Fake Slide Bytes".getBytes(StandardCharsets.UTF_8);
        repo.salvaFile("IS01/slide1.pdf", slideBytes);

        Slide slide = new Slide("slide1.pdf", "Slide", "IS01/slide1.pdf", slideBytes.length, "1", "IS01", repo, 2);
        assertEquals(2, slide.getNumeroLezione());
        assertEquals("application/pdf", slide.getMimeType());

        Dispensa dispensa = new Dispensa("dispensa.pdf", "Dispensa", "IS01/slide1.pdf", slideBytes.length, "1", "IS01", repo, "Prof. Rossi", 2026);
        assertEquals("Prof. Rossi", dispensa.getAutoreDocente());
        assertEquals(2026, dispensa.getAnnoAccademico());
    }

    @Test
    @DisplayName("Polimorfismo RisorsaLink")
    void testRisorsaLinkPolimorfismo() {
        MaterialeDidattico link = new RisorsaLink("Oracle Java", "Sito Oracle", "https://oracle.com", "1", "IS01", repo);
        assertEquals("text/html", link.getMimeType());

        AnteprimaRisultato ant = link.anteprima();
        assertEquals("https://oracle.com", ant.getUrlEsterno());
    }
}
