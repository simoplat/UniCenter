package it.project.materiale;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Polimorfismo: Specializzazione per Registrazioni e Video Lezioni.
 */
public class RisorsaVideo extends MaterialeDidattico {

    private String streamUrl;
    private int durataMinuti;

    /**
     * Costruttore per una nuova risorsa video con ID autogenerato.
     *
     * @param nome              nome della risorsa
     * @param descrizione       descrizione
     * @param streamUrl         URL di streaming video
     * @param durataMinuti      durata stimata in minuti
     * @param ownerProfessoreId ID docente proprietario
     * @param codiceMateria     codice materia
     * @param repository        repository
     */
    public RisorsaVideo(String nome, String descrizione, String streamUrl, int durataMinuti,
                        String ownerProfessoreId, String codiceMateria,
                        MaterialeDidatticoRepository repository) {
        super(nome, descrizione, streamUrl, 0, ownerProfessoreId, codiceMateria, TipoMateriale.VIDEO, repository);
        this.streamUrl = streamUrl;
        this.durataMinuti = durataMinuti;
    }

    /**
     * Costruttore completo con ID e timestamp per risorsa video.
     *
     * @param id                ID univoco
     * @param nome              nome risorsa
     * @param descrizione       descrizione
     * @param streamUrl         URL di streaming
     * @param durataMinuti      durata in minuti
     * @param dataCreazione     data di creazione
     * @param ownerProfessoreId ID docente proprietario
     * @param codiceMateria     codice materia
     * @param repository        repository
     */
    public RisorsaVideo(String id, String nome, String descrizione, String streamUrl, int durataMinuti,
                        LocalDateTime dataCreazione, String ownerProfessoreId,
                        String codiceMateria, MaterialeDidatticoRepository repository) {
        super(id, nome, descrizione, streamUrl, dataCreazione, 0, ownerProfessoreId, codiceMateria, TipoMateriale.VIDEO, repository);
        this.streamUrl = streamUrl;
        this.durataMinuti = durataMinuti;
    }

    /**
     * Restituisce l'URL di streaming del video.
     *
     * @return URL streaming
     */
    public String getStreamUrl() { return streamUrl; }

    /**
     * Imposta l'URL di streaming del video.
     *
     * @param streamUrl nuovo URL streaming
     */
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    /**
     * Restituisce la durata del video in minuti.
     *
     * @return durata minuti
     */
    public int getDurataMinuti() { return durataMinuti; }

    /**
     * Imposta la durata del video in minuti.
     *
     * @param durataMinuti durata minuti
     */
    public void setDurataMinuti(int durataMinuti) { this.durataMinuti = durataMinuti; }

    @Override
    public AnteprimaRisultato anteprima() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("streamUrl", streamUrl);
        extra.put("durataMinuti", durataMinuti);
        extra.put("visualizzatore", "video-player");

        return new AnteprimaRisultato(
                getId(),
                getNome(),
                getDescrizione(),
                getTipo(),
                getMimeType(),
                "Video lezione (" + durataMinuti + " min). Streaming disponibile all'indirizzo: " + streamUrl,
                streamUrl,
                null,
                0,
                extra
        );
    }

    @Override
    public byte[] scarica() {
        if (repository != null && pathRelativo != null && !pathRelativo.startsWith("http")) {
            try {
                return repository.leggiFile(pathRelativo);
            } catch (Exception ignored) {}
        }
        return new byte[0];
    }

    @Override
    public String getMimeType() {
        return TipoMateriale.VIDEO.getMimeType();
    }
}
