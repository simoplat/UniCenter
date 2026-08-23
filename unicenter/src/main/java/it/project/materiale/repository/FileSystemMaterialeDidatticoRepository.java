package it.project.materiale.repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

/**
 * Implementazione concreta su FileSystem di MaterialeDidatticoRepository.
 * Gestisce la creazione e conservazione fisica di cartelle e file all'interno
 * della cartella radice 'materiale_didattico/'.
 */
public class FileSystemMaterialeDidatticoRepository implements MaterialeDidatticoRepository {

    private final Path rootPath;

    /**
     * Costruttore di default. Risolve il percorso radice standard "materiale_didattico".
     */
    public FileSystemMaterialeDidatticoRepository() {
        this(resolveDefaultRootPath());
    }

    /**
     * Costruttore con percorso radice personalizzato.
     *
     * @param customRoot percorso radice personalizzato
     */
    public FileSystemMaterialeDidatticoRepository(Path customRoot) {
        this.rootPath = customRoot.toAbsolutePath().normalize();
        try {
            if (!Files.exists(this.rootPath)) {
                Files.createDirectories(this.rootPath);
            }
        } catch (IOException e) {
            System.err.println("[REPO ERROR] Impossibile creare cartella radice materiale didattico: " + e.getMessage());
        }
    }

    private static Path resolveDefaultRootPath() {
        Path[] candidates = new Path[]{
                Paths.get("materiale_didattico"),
                Paths.get("unicenter/materiale_didattico"),
                Paths.get("../materiale_didattico")
        };
        for (Path p : candidates) {
            if (Files.exists(p) && Files.isDirectory(p)) {
                return p.toAbsolutePath().normalize();
            }
        }
        // Se non esiste ancora, creiamo 'materiale_didattico' nella working directory
        return Paths.get("materiale_didattico").toAbsolutePath().normalize();
    }

    private Path resolveSafePath(String pathRelativo) {
        if (pathRelativo == null || pathRelativo.trim().isEmpty()) {
            return rootPath;
        }
        // Sanitizzazione base contro path-traversal
        String sanitized = pathRelativo.replace("\\", "/");
        while (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }
        Path target = rootPath.resolve(sanitized).normalize();
        if (!target.startsWith(rootPath)) {
            throw new SecurityException("Tentativo di accesso illegale fuori dalla cartella materiale didattico: " + pathRelativo);
        }
        return target;
    }

    @Override
    public void salvaFile(String pathRelativo, byte[] contenuto) throws IOException {
        Path target = resolveSafePath(pathRelativo);
        if (target.getParent() != null && !Files.exists(target.getParent())) {
            Files.createDirectories(target.getParent());
        }
        Files.write(target, contenuto != null ? contenuto : new byte[0],
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    @Override
    public byte[] leggiFile(String pathRelativo) throws IOException {
        Path target = resolveSafePath(pathRelativo);
        if (!Files.exists(target)) {
            throw new NoSuchFileException("File non trovato su disco: " + pathRelativo);
        }
        return Files.readAllBytes(target);
    }

    @Override
    public boolean eliminaFile(String pathRelativo) {
        try {
            Path target = resolveSafePath(pathRelativo);
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void creaDirectory(String pathRelativo) {
        try {
            Path target = resolveSafePath(pathRelativo);
            if (!Files.exists(target)) {
                Files.createDirectories(target);
            }
        } catch (IOException e) {
            System.err.println("[REPO ERROR] Errore creazione directory " + pathRelativo + ": " + e.getMessage());
        }
    }

    @Override
    public boolean eliminaDirectory(String pathRelativo) {
        try {
            Path target = resolveSafePath(pathRelativo);
            if (!Files.exists(target)) return false;
            // Cancellazione ricorsiva
            try (var walk = Files.walk(target)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    });
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean esiste(String pathRelativo) {
        Path target = resolveSafePath(pathRelativo);
        return Files.exists(target);
    }

    @Override
    public long getDimensioneFile(String pathRelativo) {
        try {
            Path target = resolveSafePath(pathRelativo);
            if (Files.exists(target) && Files.isRegularFile(target)) {
                return Files.size(target);
            }
        } catch (IOException ignored) {}
        return 0L;
    }

    @Override
    public Path getRootPath() {
        return rootPath;
    }
}
