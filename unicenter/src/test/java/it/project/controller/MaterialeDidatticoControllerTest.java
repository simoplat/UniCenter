package it.project.controller;

import it.project.Materia;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.materiale.*;
import it.project.materiale.repository.FileSystemMaterialeDidatticoRepository;
import it.project.materiale.repository.MaterialeDidatticoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Unitari - MaterialeDidatticoController (UC6 Professore & UC10 Studente)")
public class MaterialeDidatticoControllerTest {

    private Unicenter unicenter;
    private GestoreMaterieController gestoreMaterie;
    private MaterialeDidatticoController controller;
    private MaterialeDidatticoRepository repo;

    private Professore profRossi;
    private Professore profVerdi;
    private Studente studente;
    private Materia materiaIS;
    private Materia materiaSO;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("unicenter_ctrl_test");
        repo = new FileSystemMaterialeDidatticoRepository(tempDir);

        unicenter = Unicenter.getInstance();
        gestoreMaterie = unicenter.getGestoreMaterie();
        controller = new MaterialeDidatticoController(unicenter, gestoreMaterie, repo);

        // Setup Professori
        profRossi = new Professore("1", "Mario", "Rossi", "mario.rossi@unicenter.it", "password", "RSSMRA80A01H501U");
        profVerdi = new Professore("2", "Giuseppe", "Verdi", "giuseppe.verdi@unicenter.it", "password", "VRDGPP80A01H501U");

        // Setup Studente
        studente = new Studente("MAT-10001", "Luca", "Bianchi", "luca.bianchi@studenti.it", "password", "BNCLCU00A01H501Z", "ING_INF");

        // Setup Materie
        materiaIS = new Materia("IS01", "Ingegneria del Software", 9);
        materiaSO = new Materia("SO01", "Sistemi Operativi", 9);

        gestoreMaterie.addMateria(materiaIS);
        gestoreMaterie.addMateria(materiaSO);

        // Abilitazioni
        gestoreMaterie.associaProfessoreAMateria("1", "IS01");
        gestoreMaterie.associaProfessoreAMateria("2", "SO01");
    }

    @Test
    @DisplayName("UC6: Professore crea sottocartella e carica materiale per la propria materia")
    void testProfessoreCreaCartellaECaricaMateriale() {
        // 1. Creazione cartella
        Cartella cartellaSlide = controller.creaCartella(
                profRossi, "IS01", null, "Slide_2026", "Slide del nuovo anno accademico");
        assertNotNull(cartellaSlide);
        assertEquals("Slide_2026", cartellaSlide.getNome());
        assertEquals("1", cartellaSlide.getOwnerProfessoreId());

        // 2. Caricamento file PDF
        byte[] pdfContent = "%PDF-1.4 Fake Content".getBytes(StandardCharsets.UTF_8);
        MaterialeDidattico slide = controller.caricaMateriale(
                profRossi, "IS01", cartellaSlide.getId(), "Lezione_01.pdf",
                "Prima lezione", TipoMateriale.SLIDE, pdfContent);

        assertNotNull(slide);
        assertEquals("Lezione_01.pdf", slide.getNome());
        assertEquals("1", slide.getOwnerProfessoreId());
        assertTrue(repo.esiste(slide.getPathRelativo()));

        // 3. Verifica albero materia
        Cartella albero = controller.getAlberoMateria("IS01");
        assertNotNull(albero.trovaElemento(slide.getId()));
    }

    @Test
    @DisplayName("UC6: Professore non abilitato alla materia riceve SecurityException")
    void testProfessoreNonAbilitatoLanciaEccezione() {
        // Prof. Verdi non insegna IS01
        assertThrows(SecurityException.class, () ->
                controller.creaCartella(profVerdi, "IS01", null, "Cartella_Illegale", ""));

        assertThrows(SecurityException.class, () ->
                controller.caricaMateriale(profVerdi, "IS01", null, "File.txt", "", TipoMateriale.TESTO, new byte[10]));
    }

    @Test
    @DisplayName("UC6: Professore non può scrivere nella cartella di un altro docente")
    void testProfessoreNonPuoScrivereInCartellaAltroDocente() {
        // Associa anche Verdi a IS01 come secondo docente
        gestoreMaterie.associaProfessoreAMateria("2", "IS01");

        // Rossi crea una propria cartella
        Cartella cartellaRossi = controller.creaCartella(
                profRossi, "IS01", null, "Appunti_Rossi", "");

        // Verdi tenta di creare una sottocartella dentro la cartella di Rossi
        assertThrows(SecurityException.class, () ->
                controller.creaCartella(profVerdi, "IS01", cartellaRossi.getId(), "Invasione", ""));
    }

    @Test
    @DisplayName("UC6: Eliminazione materiale didattico da parte del docente proprietario")
    void testEliminaMaterialeDidattico() {
        Cartella cartella = controller.creaCartella(profRossi, "IS01", null, "DaEliminare", "");
        MaterialeDidattico mat = controller.caricaMateriale(
                profRossi, "IS01", cartella.getId(), "temp.txt", "da cancellare", TipoMateriale.TESTO, "delete me".getBytes());

        assertTrue(repo.esiste(mat.getPathRelativo()));

        // Eliminazione file
        boolean eliminato = controller.eliminaElemento(profRossi, "IS01", mat.getId());
        assertTrue(eliminato);
        assertFalse(repo.esiste(mat.getPathRelativo()));

        // Eliminazione cartella
        boolean cartellaEliminata = controller.eliminaElemento(profRossi, "IS01", cartella.getId());
        assertTrue(cartellaEliminata);
    }

    @Test
    @DisplayName("UC10: Studente consulta anteprima e scarica materiale")
    void testStudenteConsultaEScarica() {
        String testo = "Contenuto appunti per UC10";
        MaterialeDidattico txt = controller.caricaMateriale(
                profRossi, "IS01", null, "Appunti_UC10.txt", "Note", TipoMateriale.TESTO, testo.getBytes(StandardCharsets.UTF_8));

        // Anteprima polimorfica
        AnteprimaRisultato ant = controller.consultaMateriale(txt.getId());
        assertNotNull(ant);
        assertEquals("text/plain", ant.getMimeType());
        assertEquals(testo, ant.getContenutoTestuale());

        // Download
        MaterialeDidatticoController.DownloadResponse resp = controller.scaricaMateriale(txt.getId());
        assertNotNull(resp);
        assertEquals("Appunti_UC10.txt", resp.getNomeFile());
        assertEquals("text/plain", resp.getMimeType());
        assertEquals(testo, new String(resp.getBytes(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("UC10: Studente aggiunge e rimuove preferiti")
    void testStudentePreferiti() {
        MaterialeDidattico mat = controller.caricaMateriale(
                profRossi, "IS01", null, "Dispensa_Preferita.pdf", "Importante", TipoMateriale.DISPENSA, "PDF".getBytes());

        // Aggiunge ai preferiti
        boolean pref1 = controller.togglePreferito(studente, mat.getId());
        assertTrue(pref1);
        assertTrue(studente.isPreferito(mat.getId()));

        List<ElementoDidattico> preferiti = controller.getPreferitiStudente(studente);
        assertEquals(1, preferiti.size());
        assertEquals(mat.getId(), preferiti.get(0).getId());

        // Rimuove dai preferiti
        boolean pref2 = controller.togglePreferito(studente, mat.getId());
        assertFalse(pref2);
        assertFalse(studente.isPreferito(mat.getId()));
        assertEquals(0, controller.getPreferitiStudente(studente).size());
    }
}
