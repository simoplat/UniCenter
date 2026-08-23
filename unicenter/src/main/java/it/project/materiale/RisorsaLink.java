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

    /**
     * Costruttore per una nuova risorsa link con ID autogenerato.
     *
     * @param nome              nome della risorsa
     * @param descrizione       descrizione
     * @param url               indirizzo web
     * @param ownerProfessoreId ID docente proprietario
     * @param codiceMateria     codice materia
     * @param repository        repository
     */
    public RisorsaLink(String nome, String descrizione, String url,
                       String ownerProfessoreId, String codiceMateria,
                       MaterialeDidatticoRepository repository) {
        super(nome, descrizione, "", 0, ownerProfessoreId, codiceMateria, TipoMateriale.LINK, repository);
        this.url = url;
    }

    /**
     * Costruttore completo con ID e timestamp per risorsa link.
     *
     * @param id                ID univoco
     * @param nome              nome risorsa
     * @param descrizione       descrizione
     * @param url               indirizzo web
     * @param dataCreazione     data di creazione
     * @param ownerProfessoreId ID docente proprietario
     * @param codiceMateria     codice materia
     * @param repository        repository
     */
    public RisorsaLink(String id, String nome, String descrizione, String url,
                       LocalDateTime dataCreazione, String ownerProfessoreId,
                       String codiceMateria, MaterialeDidatticoRepository repository) {
        super(id, nome, descrizione, "", dataCreazione, 0, ownerProfessoreId, codiceMateria, TipoMateriale.LINK, repository);
        this.url = url;
    }

    /**
     * Restituisce l'URL di destinazione.
     *
     * @return url
     */
    public String getUrl() { return url; }

    /**
     * Imposta l'URL di destinazione.
     *
     * @param url nuovo url
     */
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
