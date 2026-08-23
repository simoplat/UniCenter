package it.project.materiale;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Polimorfismo: Specializzazione per documenti PDF.
 */
public class DocumentoPdf extends MaterialeDidattico {

    private int numeroPagine;

    /**
     * Costruttore per un nuovo documento PDF.
     *
     * @param nome              nome del file
     * @param descrizione       descrizione
     * @param pathRelativo      percorso relativo
     * @param dimensioneBytes   dimensione in byte
     * @param ownerProfessoreId id docente proprietario
     * @param codiceMateria     codice materia
     * @param repository        repository
     */
    public DocumentoPdf(String nome, String descrizione, String pathRelativo,
                        long dimensioneBytes, String ownerProfessoreId,
                        String codiceMateria, MaterialeDidatticoRepository repository) {
        super(nome, descrizione, pathRelativo, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.PDF, repository);
        this.numeroPagine = 1;
    }

    /**
     * Costruttore completo con ID e numero pagine.
     *
     * @param id                ID univoco
     * @param nome              nome file
     * @param descrizione       descrizione
     * @param pathRelativo      percorso relativo
     * @param dataCreazione     data di creazione
     * @param dimensioneBytes   dimensione in byte
     * @param ownerProfessoreId id docente proprietario
     * @param codiceMateria     codice materia
     * @param repository        repository
     * @param numeroPagine      numero stimato di pagine
     */
    public DocumentoPdf(String id, String nome, String descrizione, String pathRelativo,
                        LocalDateTime dataCreazione, long dimensioneBytes,
                        String ownerProfessoreId, String codiceMateria,
                        MaterialeDidatticoRepository repository, int numeroPagine) {
        super(id, nome, descrizione, pathRelativo, dataCreazione, dimensioneBytes, ownerProfessoreId, codiceMateria, TipoMateriale.PDF, repository);
        this.numeroPagine = numeroPagine > 0 ? numeroPagine : 1;
    }

    /**
     * Restituisce il numero di pagine del documento PDF.
     *
     * @return numero pagine
     */
    public int getNumeroPagine() { return numeroPagine; }

    /**
     * Imposta il numero di pagine del documento PDF.
     *
     * @param numeroPagine numero pagine
     */
    public void setNumeroPagine(int numeroPagine) { this.numeroPagine = numeroPagine; }

    @Override
    public AnteprimaRisultato anteprima() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("numeroPagine", numeroPagine);
        extra.put("visualizzatore", "pdf-viewer");
        extra.put("formato", "PDF Document");

        String downloadUrl = "/api/materiale/download?id=" + getId();

        return new AnteprimaRisultato(
                getId(),
                getNome(),
                getDescrizione(),
                getTipo(),
                getMimeType(),
                "Documento PDF (" + (getDimensioneBytes() / 1024 + 1) + " KB). Usa il pulsante 'Apri / Scarica' o il visualizzatore integrato.",
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
                System.err.println("[PDF READ ERROR] " + e.getMessage());
            }
        }
        return new byte[0];
    }

    @Override
    public String getMimeType() {
        return TipoMateriale.PDF.getMimeType();
    }
}
