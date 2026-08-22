package it.project.materiale;

/**
 * Tipologie supportate di materiale didattico.
 */
public enum TipoMateriale {
    PDF("Documento PDF", "application/pdf", "📄"),
    TESTO("File di Testo", "text/plain", "📝"),
    SLIDE("Slide / Presentazione", "application/pdf", "📊"),
    DISPENSA("Dispensa del Corso", "application/pdf", "📚"),
    LINK("Link / Risorsa Web", "text/html", "🔗"),
    VIDEO("Video / Registrazione", "video/mp4", "🎥");

    private final String descrizione;
    private final String mimeType;
    private final String icona;

    TipoMateriale(String descrizione, String mimeType, String icona) {
        this.descrizione = descrizione;
        this.mimeType = mimeType;
        this.icona = icona;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getIcona() {
        return icona;
    }
}
