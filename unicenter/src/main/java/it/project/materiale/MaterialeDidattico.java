package it.project.materiale;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import it.project.database.ClockProvider;
import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Superclasse astratta per tutti i materiali didattici (Pattern GoF Composite - Foglia).
 * Fornisce il contratto polimorfico (Pattern GRASP Polymorphism) per anteprima e download.
 */
public abstract class MaterialeDidattico implements ElementoDidattico {

    /** Identificativo univoco della risorsa didattica. */
    protected String id;
    /** Nome o titolo della risorsa. */
    protected String nome;
    /** Descrizione o annotazioni didattiche. */
    protected String descrizione;
    /** Percorso relativo nel repository di memorizzazione. */
    protected String pathRelativo;
    /** Data e ora di creazione o caricamento. */
    protected LocalDateTime dataCreazione;
    /** Dimensione in byte del file. */
    protected long dimensioneBytes;
    /** Identificativo del professore proprietario del materiale. */
    protected String ownerProfessoreId;
    /** Codice della materia cui afferisce il materiale. */
    protected String codiceMateria;
    /** Tipologia specifica del materiale didattico. */
    protected TipoMateriale tipo;
    /** Repository di persistenza per il salvataggio e recupero fisico dei file. */
    protected transient MaterialeDidatticoRepository repository;

    /**
     * Costruttore base per un nuovo materiale con ID autogenerato.
     *
     * @param nome              nome del file/risorsa
     * @param descrizione       descrizione
     * @param pathRelativo      percorso relativo
     * @param dimensioneBytes   dimensione in byte
     * @param ownerProfessoreId ID professore proprietario
     * @param codiceMateria     codice materia
     * @param tipo              tipo di materiale
     * @param repository        repository di persistenza
     */
    public MaterialeDidattico(String nome, String descrizione, String pathRelativo,
                              long dimensioneBytes, String ownerProfessoreId,
                              String codiceMateria, TipoMateriale tipo,
                              MaterialeDidatticoRepository repository) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.descrizione = descrizione != null ? descrizione : "";
        this.pathRelativo = pathRelativo;
        this.dimensioneBytes = dimensioneBytes;
        this.ownerProfessoreId = ownerProfessoreId;
        this.codiceMateria = codiceMateria;
        this.tipo = tipo;
        this.dataCreazione = ClockProvider.nowLocalDateTime();
        this.repository = repository;
    }

    /**
     * Costruttore completo con ID e timestamp specifici.
     *
     * @param id                ID univoco
     * @param nome              nome file
     * @param descrizione       descrizione
     * @param pathRelativo      percorso relativo
     * @param dataCreazione     data di creazione
     * @param dimensioneBytes   dimensione in byte
     * @param ownerProfessoreId ID docente proprietario
     * @param codiceMateria     codice materia
     * @param tipo              tipo di materiale
     * @param repository        repository
     */
    public MaterialeDidattico(String id, String nome, String descrizione, String pathRelativo,
                              LocalDateTime dataCreazione, long dimensioneBytes,
                              String ownerProfessoreId, String codiceMateria,
                              TipoMateriale tipo, MaterialeDidatticoRepository repository) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.nome = nome;
        this.descrizione = descrizione != null ? descrizione : "";
        this.pathRelativo = pathRelativo;
        this.dimensioneBytes = dimensioneBytes;
        this.ownerProfessoreId = ownerProfessoreId;
        this.codiceMateria = codiceMateria;
        this.tipo = tipo;
        this.dataCreazione = dataCreazione != null ? dataCreazione : ClockProvider.nowLocalDateTime();
        this.repository = repository;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getNome() { return nome; }

    @Override
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public String getDescrizione() { return descrizione; }

    /**
     * Imposta la descrizione del materiale didattico.
     *
     * @param descrizione nuova descrizione
     */
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    @Override
    public String getPathRelativo() { return pathRelativo; }

    @Override
    public void setPathRelativo(String pathRelativo) { this.pathRelativo = pathRelativo; }

    @Override
    public LocalDateTime getDataCreazione() { return dataCreazione; }

    @Override
    public long getDimensioneBytes() { return dimensioneBytes; }

    /**
     * Imposta la dimensione in byte del file.
     *
     * @param dimensioneBytes dimensione in byte
     */
    public void setDimensioneBytes(long dimensioneBytes) { this.dimensioneBytes = dimensioneBytes; }

    @Override
    public boolean isCartella() { return false; }

    @Override
    public String getOwnerProfessoreId() { return ownerProfessoreId; }

    @Override
    public String getCodiceMateria() { return codiceMateria; }

    /**
     * Restituisce la tipologia di materiale.
     *
     * @return TipoMateriale
     */
    public TipoMateriale getTipo() { return tipo; }

    /**
     * Imposta il repository di persistenza.
     *
     * @param repository repository dei file
     */
    public void setRepository(MaterialeDidatticoRepository repository) {
        this.repository = repository;
    }

    /**
     * Restituisce il repository di persistenza.
     *
     * @return MaterialeDidatticoRepository
     */
    public MaterialeDidatticoRepository getRepository() {
        return repository;
    }

    @Override
    public List<ElementoDidattico> elenca() {
        return Collections.emptyList();
    }

    @Override
    public AnteprimaRisultato visualizza() {
        return anteprima();
    }

    /**
     * Metodo Polimorfico (Polymorphism):
     * Restituisce l'anteprima specifica per il tipo di materiale didattico.
     *
     * @return AnteprimaRisultato specifica
     */
    public abstract AnteprimaRisultato anteprima();

    /**
     * Metodo Polimorfico (Polymorphism):
     * Restituisce i byte per il download del materiale didattico.
     *
     * @return array di byte
     */
    public abstract byte[] scarica();

    /**
     * Metodo Polimorfico (Polymorphism):
     * Restituisce il Mime-Type specifico.
     *
     * @return stringa mime-type
     */
    public abstract String getMimeType();
}
