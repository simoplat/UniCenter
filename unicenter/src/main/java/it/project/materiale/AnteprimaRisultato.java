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

    /**
     * Costruttore completo dell'anteprima.
     *
     * @param id               identificativo univoco
     * @param nome             nome della risorsa
     * @param descrizione      descrizione
     * @param tipo             tipologia materiale
     * @param mimeType         tipo MIME
     * @param contenutoTestuale estratto testuale
     * @param urlEsterno       eventuale URL esterno
     * @param downloadUrl      URL relativo di download
     * @param dimensioneBytes  dimensione in byte
     * @param metadatiExtra    metadati aggiuntivi specifici
     */
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

    /**
     * Restituisce l'ID dell'elemento.
     *
     * @return id elemento
     */
    public String getId() { return id; }

    /**
     * Restituisce il nome dell'elemento.
     *
     * @return nome
     */
    public String getNome() { return nome; }

    /**
     * Restituisce la descrizione dell'elemento.
     *
     * @return descrizione
     */
    public String getDescrizione() { return descrizione; }

    /**
     * Restituisce il tipo di materiale.
     *
     * @return TipoMateriale
     */
    public TipoMateriale getTipo() { return tipo; }

    /**
     * Restituisce il tipo MIME.
     *
     * @return mimeType
     */
    public String getMimeType() { return mimeType; }

    /**
     * Restituisce il contenuto o estratto testuale.
     *
     * @return contenuto testuale
     */
    public String getContenutoTestuale() { return contenutoTestuale; }

    /**
     * Restituisce l'URL esterno se presente.
     *
     * @return urlEsterno
     */
    public String getUrlEsterno() { return urlEsterno; }

    /**
     * Restituisce l'URL di download.
     *
     * @return downloadUrl
     */
    public String getDownloadUrl() { return downloadUrl; }

    /**
     * Restituisce la dimensione in byte.
     *
     * @return dimensioneBytes
     */
    public long getDimensioneBytes() { return dimensioneBytes; }

    /**
     * Restituisce la mappa dei metadati extra non modificabile.
     *
     * @return mappa metadati
     */
    public Map<String, Object> getMetadatiExtra() { return Collections.unmodifiableMap(metadatiExtra); }
}
