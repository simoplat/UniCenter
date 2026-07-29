package it.project;

import java.time.LocalDateTime;

public class Notifica {
    private String oggetto;
    private String messaggio;
    private LocalDateTime dataOra;

    public Notifica(String oggetto, String messaggio, LocalDateTime dataOra) {
        this.oggetto = oggetto;
        this.messaggio = messaggio;
        this.dataOra = dataOra;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    @Override
    public String toString() {
        return "Notifica: [oggetto=" + oggetto + "]\n" +
                "[Messaggio=" + messaggio + "]\n" +  
                "[Data e ora=" + dataOra + "]\n";
    }

    
    
}
