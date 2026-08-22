package it.project.materiale;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Polimorfismo: Specializzazione per file di testo semplice (.txt, .md, note).
 */
public class FileTesto extends MaterialeDidattico {

    public FileTesto(String nome, String descrizione, String pathRelativo,
                     long dimensioneBytes, String ownerProfessoreId,
                     String codiceMateria, MaterialeDidatticoRepository repository) {
        super(nome, descrizione, pathRelativo, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.TESTO, repository);
    }

    public FileTesto(String id, String nome, String descrizione, String pathRelativo,
                     LocalDateTime dataCreazione, long dimensioneBytes,
                     String ownerProfessoreId, String codiceMateria,
                     MaterialeDidatticoRepository repository) {
        super(id, nome, descrizione, pathRelativo, dataCreazione, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.TESTO, repository);
    }

    @Override
    public AnteprimaRisultato anteprima() {
        String testo = "";
        if (repository != null && pathRelativo != null) {
            try {
                byte[] bytes = repository.leggiFile(pathRelativo);
                testo = new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                testo = "[Errore nella lettura del file di testo: " + e.getMessage() + "]";
            }
        }

        Map<String, Object> extra = new HashMap<>();
        extra.put("visualizzatore", "text-viewer");
        extra.put("lineeTotali", testo.lines().count());

        String downloadUrl = "/api/materiale/download?id=" + getId();

        return new AnteprimaRisultato(
                getId(),
                getNome(),
                getDescrizione(),
                getTipo(),
                getMimeType(),
                testo,
                null,
                downloadUrl,
                getDimensioneBytes(),
                extra
        );
    }

    @Override
    public byte[] scarica() {
        if (repository != null && pathRelativo != null) {
            try {
                return repository.leggiFile(pathRelativo);
            } catch (IOException e) {
                System.err.println("[TXT READ ERROR] " + e.getMessage());
            }
        }
        return new byte[0];
    }

    @Override
    public String getMimeType() {
        return TipoMateriale.TESTO.getMimeType();
    }
}
