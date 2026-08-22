package it.project.materiale;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Polimorfismo: Specializzazione per Slide e Presentazioni del corso.
 */
public class Slide extends MaterialeDidattico {

    private int numeroLezione;

    public Slide(String nome, String descrizione, String pathRelativo,
                 long dimensioneBytes, String ownerProfessoreId,
                 String codiceMateria, MaterialeDidatticoRepository repository, int numeroLezione) {
        super(nome, descrizione, pathRelativo, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.SLIDE, repository);
        this.numeroLezione = numeroLezione;
    }

    public Slide(String id, String nome, String descrizione, String pathRelativo,
                 LocalDateTime dataCreazione, long dimensioneBytes,
                 String ownerProfessoreId, String codiceMateria,
                 MaterialeDidatticoRepository repository, int numeroLezione) {
        super(id, nome, descrizione, pathRelativo, dataCreazione, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.SLIDE, repository);
        this.numeroLezione = numeroLezione;
    }

    public int getNumeroLezione() { return numeroLezione; }
    public void setNumeroLezione(int numeroLezione) { this.numeroLezione = numeroLezione; }

    @Override
    public AnteprimaRisultato anteprima() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("numeroLezione", numeroLezione);
        extra.put("categoria", "Slide Lezione " + (numeroLezione > 0 ? ("#" + numeroLezione) : ""));

        String downloadUrl = "/api/materiale/download?id=" + getId();

        return new AnteprimaRisultato(
                getId(),
                getNome(),
                getDescrizione(),
                getTipo(),
                getMimeType(),
                "Slide del corso" + (numeroLezione > 0 ? " (Lezione " + numeroLezione + ")" : "") + ". Fai clic per scaricare o visualizzare.",
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
                System.err.println("[SLIDE READ ERROR] " + e.getMessage());
            }
        }
        return new byte[0];
    }

    @Override
    public String getMimeType() {
        return TipoMateriale.SLIDE.getMimeType();
    }
}
