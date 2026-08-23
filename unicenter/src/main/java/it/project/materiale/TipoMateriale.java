package it.project.materiale;

/**
 * Tipologie supportate di materiale didattico.
 */
public enum TipoMateriale {
    /** Documento generico in formato PDF. */
    PDF("Documento PDF", "application/pdf", "📄"),
    /** File di testo semplice o markdown. */
    TESTO("File di Testo", "text/plain", "📝"),
    /** Slide e presentazioni di lezione. */
    SLIDE("Slide / Presentazione", "application/pdf", "📊"),
    /** Dispense e testi di approfondimento. */
    DISPENSA("Dispensa del Corso", "application/pdf", "📚"),
    /** Collegamento ipertestuale o risorsa web esterna. */
    LINK("Link / Risorsa Web", "text/html", "🔗"),
    /** Registrazione video di lezioni o esercitazioni. */
    VIDEO("Video / Registrazione", "video/mp4", "🎥");

    private final String descrizione;
    private final String mimeType;
    private final String icona;

    TipoMateriale(String descrizione, String mimeType, String icona) {
        this.descrizione = descrizione;
        this.mimeType = mimeType;
        this.icona = icona;
    }

    /**
     * Restituisce la descrizione testuale del tipo.
     *
     * @return descrizione
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Restituisce il MIME type associato al formato.
     *
     * @return mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Restituisce l'icona emoji associata al tipo.
     *
     * @return icona emoji
     */
    public String getIcona() {
        return icona;
    }
}
