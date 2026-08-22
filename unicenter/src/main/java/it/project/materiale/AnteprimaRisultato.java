package it.project.materiale;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Risultato polimorfico dell'anteprima di una risorsa o cartella didattica.
 */
public class AnteprimaRisultato {
    private final String id;
    private final String nome;
    private final String descrizione;
    private final TipoMateriale tipo;
    private final String mimeType;
    private final String contenutoTestuale;
    private final String urlEsterno;
    private final String downloadUrl;
    private final long dimensioneBytes;
    private final Map<String, Object> metadatiExtra;

    public AnteprimaRisultato(String id, String nome, String descrizione, TipoMateriale tipo,
                              String mimeType, String contenutoTestuale, String urlEsterno,
                              String downloadUrl, long dimensioneBytes, Map<String, Object> metadatiExtra) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.tipo = tipo;
        this.mimeType = mimeType;
        this.contenutoTestuale = contenutoTestuale;
        this.urlEsterno = urlEsterno;
        this.downloadUrl = downloadUrl;
        this.dimensioneBytes = dimensioneBytes;
        this.metadatiExtra = metadatiExtra != null ? metadatiExtra : new HashMap<>();
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public TipoMateriale getTipo() { return tipo; }
    public String getMimeType() { return mimeType; }
    public String getContenutoTestuale() { return contenutoTestuale; }
    public String getUrlEsterno() { return urlEsterno; }
    public String getDownloadUrl() { return downloadUrl; }
    public long getDimensioneBytes() { return dimensioneBytes; }
    public Map<String, Object> getMetadatiExtra() { return Collections.unmodifiableMap(metadatiExtra); }
}
