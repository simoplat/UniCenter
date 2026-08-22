package it.project.materiale;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Superclasse astratta per tutti i materiali didattici (Pattern GoF Composite - Foglia).
 * Fornisce il contratto polimorfico (Pattern GRASP Polymorphism) per anteprima e download.
 */
public abstract class MaterialeDidattico implements ElementoDidattico {

    protected String id;
    protected String nome;
    protected String descrizione;
    protected String pathRelativo;
    protected LocalDateTime dataCreazione;
    protected long dimensioneBytes;
    protected String ownerProfessoreId;
    protected String codiceMateria;
    protected TipoMateriale tipo;
    protected transient MaterialeDidatticoRepository repository;

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
        this.dataCreazione = LocalDateTime.now();
        this.repository = repository;
    }

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
        this.dataCreazione = dataCreazione != null ? dataCreazione : LocalDateTime.now();
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

    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    @Override
    public String getPathRelativo() { return pathRelativo; }

    @Override
    public void setPathRelativo(String pathRelativo) { this.pathRelativo = pathRelativo; }

    @Override
    public LocalDateTime getDataCreazione() { return dataCreazione; }

    @Override
    public long getDimensioneBytes() { return dimensioneBytes; }

    public void setDimensioneBytes(long dimensioneBytes) { this.dimensioneBytes = dimensioneBytes; }

    @Override
    public boolean isCartella() { return false; }

    @Override
    public String getOwnerProfessoreId() { return ownerProfessoreId; }

    @Override
    public String getCodiceMateria() { return codiceMateria; }

    public TipoMateriale getTipo() { return tipo; }

    public void setRepository(MaterialeDidatticoRepository repository) {
        this.repository = repository;
    }

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
     */
    public abstract AnteprimaRisultato anteprima();

    /**
     * Metodo Polimorfico (Polymorphism):
     * Restituisce i byte per il download del materiale didattico.
     */
    public abstract byte[] scarica();

    /**
     * Metodo Polimorfico (Polymorphism):
     * Restituisce il Mime-Type specifico.
     */
    public abstract String getMimeType();
}
