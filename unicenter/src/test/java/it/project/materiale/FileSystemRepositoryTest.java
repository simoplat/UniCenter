package it.project.materiale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.project.materiale.repository.FileSystemMaterialeDidatticoRepository;
import it.project.materiale.repository.MaterialeDidatticoRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Unitari - FileSystemMaterialeDidatticoRepository (Pure Fabrication)")
public class FileSystemRepositoryTest {

    private MaterialeDidatticoRepository repo;
    private Path tempRoot;

    @BeforeEach
    void setUp() throws Exception {
        tempRoot = Files.createTempDirectory("unicenter_repo_test");
        repo = new FileSystemMaterialeDidatticoRepository(tempRoot);
    }

    @Test
    @DisplayName("Salvataggio, lettura ed esistenza file")
    void testSalvaELeggiFile() throws IOException {
        String path = "IS01/Prof_Rossi/test.txt";
        byte[] data = "Hello UniCenter Didattica".getBytes(StandardCharsets.UTF_8);

        repo.salvaFile(path, data);

        assertTrue(repo.esiste(path));
        assertEquals(data.length, repo.getDimensioneFile(path));
        assertArrayEquals(data, repo.leggiFile(path));
    }

    @Test
    @DisplayName("Eliminazione file e directory")
    void testEliminaFileEDirectory() throws IOException {
        String dir = "IS01/Prof_Rossi";
        String file = "IS01/Prof_Rossi/sub/test.txt";
        repo.salvaFile(file, "content".getBytes());

        assertTrue(repo.esiste(file));
        assertTrue(repo.eliminaFile(file));
        assertFalse(repo.esiste(file));

        repo.salvaFile(file, "content2".getBytes());
        assertTrue(repo.eliminaDirectory(dir));
        assertFalse(repo.esiste(file));
    }

    @Test
    @DisplayName("Tentativo di lettura file non esistente lancia NoSuchFileException")
    void testLetturaFileInesistente() {
        assertThrows(NoSuchFileException.class, () -> repo.leggiFile("non_esiste.txt"));
    }

    @Test
    @DisplayName("Protezione da Path Traversal")
    void testPathTraversalProtection() {
        assertThrows(SecurityException.class, () -> repo.salvaFile("../../secret.txt", new byte[0]));
    }
}
