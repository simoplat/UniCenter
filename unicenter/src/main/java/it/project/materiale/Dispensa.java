package it.project.materiale;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Polimorfismo: Specializzazione per Dispense didattiche complete e compendi.
 */
public class Dispensa extends MaterialeDidattico {

    private String autoreDocente;
    private int annoAccademico;

    public Dispensa(String nome, String descrizione, String pathRelativo,
                    long dimensioneBytes, String ownerProfessoreId,
                    String codiceMateria, MaterialeDidatticoRepository repository,
                    String autoreDocente, int annoAccademico) {
        super(nome, descrizione, pathRelativo, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.DISPENSA, repository);
        this.autoreDocente = autoreDocente != null ? autoreDocente : "Docente del Corso";
        this.annoAccademico = annoAccademico > 0 ? annoAccademico : 2026;
    }

    public Dispensa(String id, String nome, String descrizione, String pathRelativo,
                    LocalDateTime dataCreazione, long dimensioneBytes,
                    String ownerProfessoreId, String codiceMateria,
                    MaterialeDidatticoRepository repository,
                    String autoreDocente, int annoAccademico) {
        super(id, nome, descrizione, pathRelativo, dataCreazione, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.DISPENSA, repository);
        this.autoreDocente = autoreDocente != null ? autoreDocente : "Docente del Corso";
        this.annoAccademico = annoAccademico > 0 ? annoAccademico : 2026;
    }

    public String getAutoreDocente() { return autoreDocente; }
    public void setAutoreDocente(String autoreDocente) { this.autoreDocente = autoreDocente; }
    public int getAnnoAccademico() { return annoAccademico; }
    public void setAnnoAccademico(int annoAccademico) { this.annoAccademico = annoAccademico; }

    @Override
    public AnteprimaRisultato anteprima() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("autore", autoreDocente);
        extra.put("annoAccademico", annoAccademico);
        extra.put("categoria", "Dispensa Ufficiale");

        String downloadUrl = "/api/materiale/download?id=" + getId();

        return new AnteprimaRisultato(
                getId(),
                getNome(),
                getDescrizione(),
                getTipo(),
                getMimeType(),
                "Dispensa del corso redatta da " + autoreDocente + " (A.A. " + annoAccademico + ").",
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
                System.err.println("[DISPENSA READ ERROR] " + e.getMessage());
            }
        }
        return new byte[0];
    }

    @Override
    public String getMimeType() {
        return TipoMateriale.DISPENSA.getMimeType();
    }
}
