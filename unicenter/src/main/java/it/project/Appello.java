package it.project;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appello {

    private int idAppello;
    private LocalDate dataAppello;
    private String aulaAppello;
    private LocalTime orario;
    private int postiDisponibili;
    private int postiPrenotati;
    private String vincolo;
    private int idMateria;
    
    public Appello(int idAppello, LocalDate dataAppello, String aulaAppello, LocalTime orario, int postiDisponibili,
            int postiPrenotati, String vincolo, int idMateria) {
        this.idAppello = idAppello;
        this.dataAppello = dataAppello;
        this.aulaAppello = aulaAppello;
        this.orario = orario;
        this.postiDisponibili = postiDisponibili;
        this.postiPrenotati = postiPrenotati;
        this.vincolo = vincolo;
        this.idMateria = idMateria;
    }

    public int getIdAppello() {
        return idAppello;
    }

    public LocalDate getDataAppello() {
        return dataAppello;
    }

    public void setDataAppello(LocalDate dataAppello) {
        this.dataAppello = dataAppello;
    }

    public String getAulaAppello() {
        return aulaAppello;
    }

    public void setAulaAppello(String aulaAppello) {
        this.aulaAppello = aulaAppello;
    }

    public LocalTime getOrario() {
        return orario;
    }

    public void setOrario(LocalTime orario) {
        this.orario = orario;
    }

    public int getPostiDisponibili() {
        return postiDisponibili;
    }

    public void setPostiDisponibili(int postiDisponibili) {
        this.postiDisponibili = postiDisponibili;
    }

    public int getPostiPrenotati() {
        return postiPrenotati;
    }

    public void setPostiPrenotati(int postiPrenotati) {
        this.postiPrenotati = postiPrenotati;
    }

    public String getVincolo() {
        return vincolo;
    }

    public void setVincolo(String vincolo) {
        this.vincolo = vincolo;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }
    
    

}
