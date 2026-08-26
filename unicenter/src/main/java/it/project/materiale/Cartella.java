package it.project.materiale;

import java.time.LocalDateTime;
import java.util.*;

import it.project.database.ClockProvider;
import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Composite (GoF Structural Pattern):
 * Rappresenta un nodo cartella capace di contenere ricorsivamente altri ElementiDidattici (Cartelle o Materiali).
 * Agisce anche da Creator (GRASP) per la creazione di elementi contenuti al suo interno.
 */
public class Cartella implements ElementoDidattico {

    private String id;
    private String nome;
    private String descrizione;
    private String pathRelativo;
    private LocalDateTime dataCreazione;
    private String ownerProfessoreId;
    private String codiceMateria;
    private final List<ElementoDidattico> elementi;

    /**
     * Costruttore per una nuova cartella con ID autogenerato.
     *
     * @param nome              nome della cartella
     * @param descrizione       descrizione
     * @param pathRelativo      percorso relativo
     * @param ownerProfessoreId ID professore proprietario
     * @param codiceMateria     codice materia
     */
    public Cartella(String nome, String descrizione, String pathRelativo,
                    String ownerProfessoreId, String codiceMateria) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.descrizione = descrizione != null ? descrizione : "";
        this.pathRelativo = pathRelativo != null ? pathRelativo : "";
        this.ownerProfessoreId = ownerProfessoreId;
        this.codiceMateria = codiceMateria;
        this.dataCreazione = ClockProvider.nowLocalDateTime();
        this.elementi = new ArrayList<>();
    }

    /**
     * Costruttore completo con ID e timestamp specifici.
     *
     * @param id                ID univoco
     * @param nome              nome cartella
     * @param descrizione       descrizione
     * @param pathRelativo      percorso relativo
     * @param dataCreazione     data di creazione
     * @param ownerProfessoreId ID professore proprietario
     * @param codiceMateria     codice materia
     */
    public Cartella(String id, String nome, String descrizione, String pathRelativo,
                    LocalDateTime dataCreazione, String ownerProfessoreId, String codiceMateria) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.nome = nome;
        this.descrizione = descrizione != null ? descrizione : "";
        this.pathRelativo = pathRelativo != null ? pathRelativo : "";
        this.ownerProfessoreId = ownerProfessoreId;
        this.codiceMateria = codiceMateria;
        this.dataCreazione = dataCreazione != null ? dataCreazione : ClockProvider.nowLocalDateTime();
        this.elementi = new ArrayList<>();
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
     * Imposta la descrizione della cartella.
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
    public boolean isCartella() { return true; }

    @Override
    public String getOwnerProfessoreId() { return ownerProfessoreId; }

    /**
     * Imposta l'ID del professore proprietario della cartella.
     *
     * @param ownerProfessoreId ID docente
     */
    public void setOwnerProfessoreId(String ownerProfessoreId) { this.ownerProfessoreId = ownerProfessoreId; }

    @Override
    public String getCodiceMateria() { return codiceMateria; }

    @Override
    public List<ElementoDidattico> elenca() {
        return Collections.unmodifiableList(elementi);
    }

    /**
     * Pattern Composite: Somma ricorsiva delle dimensioni di tutti gli elementi contenuti.
     *
     * @return dimensione totale in byte
     */
    @Override
    public long getDimensioneBytes() {
        long totale = 0;
        for (ElementoDidattico e : elementi) {
            totale += e.getDimensioneBytes();
        }
        return totale;
    }

    /**
     * Aggiunge un elemento figlio alla cartella.
     *
     * @param elemento elemento da aggiungere
     */
    public void aggiungiElemento(ElementoDidattico elemento) {
        if (elemento != null && !elementi.contains(elemento)) {
            elementi.add(elemento);
        }
    }

    /**
     * Rimuove un elemento figlio per ID (ricorsivo).
     *
     * @param idElemento id dell'elemento da rimuovere
     * @return true se rimosso, false altrimenti
     */
    public boolean rimuoviElemento(String idElemento) {
        if (idElemento == null) return false;
        // Ricerca di primo livello
        Iterator<ElementoDidattico> it = elementi.iterator();
        while (it.hasNext()) {
            ElementoDidattico e = it.next();
            if (e.getId().equals(idElemento)) {
                it.remove();
                return true;
            }
        }
        // Ricerca ricorsiva nelle sottocartelle
        for (ElementoDidattico e : elementi) {
            if (e instanceof Cartella) {
                boolean rimosso = ((Cartella) e).rimuoviElemento(idElemento);
                if (rimosso) return true;
            }
        }
        return false;
    }

    /**
     * Cerca un elemento per ID in tutta la gerarchia sottostante.
     *
     * @param idElemento id da cercare
     * @return ElementoDidattico trovato o null
     */
    public ElementoDidattico trovaElemento(String idElemento) {
        if (idElemento == null) return null;
        if (this.id.equals(idElemento)) return this;

        for (ElementoDidattico e : elementi) {
            if (e.getId().equals(idElemento)) {
                return e;
            }
            if (e instanceof Cartella) {
                ElementoDidattico trovato = ((Cartella) e).trovaElemento(idElemento);
                if (trovato != null) return trovato;
            }
        }
        return null;
    }

    /**
     * Cerca una cartella contenitore per ID.
     *
     * @param idCartella id cartella
     * @return Cartella trovata o null
     */
    public Cartella trovaCartella(String idCartella) {
        ElementoDidattico elem = trovaElemento(idCartella);
        if (elem instanceof Cartella) {
            return (Cartella) elem;
        }
        return null;
    }

    /**
     * Cerca la cartella genitore di un determinato elemento.
     *
     * @param idFiglio id dell'elemento figlio
     * @return Cartella genitore o null
     */
    public Cartella trovaCartellaGenitore(String idFiglio) {
        if (idFiglio == null) return null;
        for (ElementoDidattico e : elementi) {
            if (e.getId().equals(idFiglio)) {
                return this;
            }
            if (e instanceof Cartella) {
                Cartella genitore = ((Cartella) e).trovaCartellaGenitore(idFiglio);
                if (genitore != null) return genitore;
            }
        }
        return null;
    }

    // =========================================================================
    // CREATOR PATTERN (GRASP): Metodi di creazione di elementi figli
    // =========================================================================

    /**
     * Crea e aggiunge una nuova sottocartella all'interno di questa cartella.
     *
     * @param nomeCartella nome cartella
     * @param descrizione  descrizione
     * @param ownerProfId  id docente proprietario
     * @return nuova Cartella
     */
    public Cartella creaSubCartella(String nomeCartella, String descrizione, String ownerProfId) {
        String subPath = (pathRelativo == null || pathRelativo.isEmpty())
                ? nomeCartella
                : pathRelativo + "/" + nomeCartella;
        Cartella sub = new Cartella(nomeCartella, descrizione, subPath, ownerProfId, this.codiceMateria);
        this.aggiungiElemento(sub);
        return sub;
    }

    /**
     * Creator: Crea e aggiunge una nuova istanza di MaterialeDidattico polimorfico.
     *
     * @param nomeFile    nome file
     * @param descrizione descrizione
     * @param tipo        tipo materiale
     * @param contenuto   byte del file
     * @param ownerProfId id docente
     * @param repo        repository per la persistenza
     * @return MaterialeDidattico creato
     */
    public MaterialeDidattico creaMateriale(String nomeFile, String descrizione, TipoMateriale tipo,
                                            byte[] contenuto, String ownerProfId,
                                            MaterialeDidatticoRepository repo) {
        String subPath = (pathRelativo == null || pathRelativo.isEmpty())
                ? nomeFile
                : pathRelativo + "/" + nomeFile;
        long dimensione = (contenuto != null) ? contenuto.length : 0;

        MaterialeDidattico mat;
        switch (tipo) {
            case PDF:
                mat = new DocumentoPdf(nomeFile, descrizione, subPath, dimensione, ownerProfId, this.codiceMateria, repo);
                break;
            case TESTO:
                mat = new FileTesto(nomeFile, descrizione, subPath, dimensione, ownerProfId, this.codiceMateria, repo);
                break;
            case SLIDE:
                mat = new Slide(nomeFile, descrizione, subPath, dimensione, ownerProfId, this.codiceMateria, repo, 1);
                break;
            case DISPENSA:
                mat = new Dispensa(nomeFile, descrizione, subPath, dimensione, ownerProfId, this.codiceMateria, repo, "Docente del Corso", 2026);
                break;
            case LINK:
                String linkUrl = (contenuto != null && contenuto.length > 0) ? new String(contenuto) : subPath;
                mat = new RisorsaLink(nomeFile, descrizione, linkUrl, ownerProfId, this.codiceMateria, repo);
                break;
            case VIDEO:
                String videoUrl = (contenuto != null && contenuto.length > 0) ? new String(contenuto) : subPath;
                mat = new RisorsaVideo(nomeFile, descrizione, videoUrl, 45, ownerProfId, this.codiceMateria, repo);
                break;
            default:
                mat = new FileTesto(nomeFile, descrizione, subPath, dimensione, ownerProfId, this.codiceMateria, repo);
                break;
        }

        this.aggiungiElemento(mat);
        return mat;
    }

    @Override
    public AnteprimaRisultato visualizza() {
        int numCartelle = 0;
        int numFile = 0;
        for (ElementoDidattico e : elementi) {
            if (e.isCartella()) numCartelle++;
            else numFile++;
        }

        Map<String, Object> extra = new HashMap<>();
        extra.put("totaleElementi", elementi.size());
        extra.put("numeroCartelle", numCartelle);
        extra.put("numeroFile", numFile);
        extra.put("dimensioneTotaleBytes", getDimensioneBytes());

        return new AnteprimaRisultato(
                getId(),
                getNome(),
                getDescrizione(),
                null,
                "application/x-directory",
                "Cartella contenente " + numCartelle + " cartelle e " + numFile + " file (" + (getDimensioneBytes() / 1024) + " KB).",
                null,
                null,
                getDimensioneBytes(),
                extra
        );
    }
}
