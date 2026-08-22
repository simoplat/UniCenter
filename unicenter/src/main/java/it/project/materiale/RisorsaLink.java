package it.project.materiale;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Polimorfismo: Specializzazione per collegamenti a risorse web esterne (documentazioni, repository GitHub, siti).
 */
public class RisorsaLink extends MaterialeDidattico {

    private String url;

    public RisorsaLink(String nome, String descrizione, String url,
                       String ownerProfessoreId, String codiceMateria,
                       MaterialeDidatticoRepository repository) {
        super(nome, descrizione, "", 0, ownerProfessoreId, codiceMateria, TipoMateriale.LINK, repository);
        this.url = url;
    }

    public RisorsaLink(String id, String nome, String descrizione, String url,
                       LocalDateTime dataCreazione, String ownerProfessoreId,
                       String codiceMateria, MaterialeDidatticoRepository repository) {
        super(id, nome, descrizione, "", dataCreazione, 0, ownerProfessoreId, codiceMateria, TipoMateriale.LINK, repository);
        this.url = url;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    @Override
    public AnteprimaRisultato anteprima() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("url", url);
        extra.put("tipoLink", "Collegamento Esterno");

        return new AnteprimaRisultato(
                getId(),
                getNome(),
                getDescrizione(),
                getTipo(),
                getMimeType(),
                "Risorsa web esterna: " + url,
                url,
                null,
                0,
                extra
        );
    }

    @Override
    public byte[] scarica() {
        // Per un link web, il download fornisce un file URL di scorciatoia .url o il link in testo
        String shortcut = "[InternetShortcut]\nURL=" + url + "\n";
        return shortcut.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getMimeType() {
        return TipoMateriale.LINK.getMimeType();
    }
}
