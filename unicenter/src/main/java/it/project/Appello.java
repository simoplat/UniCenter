package it.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Appello {
    private String codiceAppello;
    private String codiceMateria;
    private LocalDateTime dataOra;
    private String aula;
    private int postiDisponibili;
    private String vincoloLetteraCognome;
    private List<Studente> iscritti;

    public Appello(String codiceAppello, String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili, String vincoloLetteraCognome) {
        this.codiceAppello = codiceAppello;
        this.codiceMateria = codiceMateria;
        this.dataOra = dataOraStr;
        this.aula = aula;
        this.postiDisponibili = postiDisponibili;
        this.vincoloLetteraCognome = vincoloLetteraCognome;
        this.iscritti = new ArrayList<>();
    }

    public synchronized void aggiungiIscritto(Studente studente) {
        if (postiDisponibili <= 0) {
            throw new IllegalStateException("Nessun posto disponibile per l'appello " + codiceAppello);
        }
        iscritti.add(studente);
        postiDisponibili--;
    }

    public String getCodiceAppello() { return codiceAppello; }
    public String getCodiceMateria() { return codiceMateria; }
    public LocalDateTime getDataOra() { return dataOra; }
    public String getAula() { return aula; }
    public int getPostiDisponibili() { return postiDisponibili; }
    public String getVincoloLetteraCognome() { return vincoloLetteraCognome; }
    public List<Studente> getIscritti() { return iscritti; }

    

    public void setCodiceMateria(String codiceMateria) {
        this.codiceMateria = codiceMateria;
    }

    public void setDataOra(LocalDateTime dataOra) {
        this.dataOra = dataOra;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public void setPostiDisponibili(int postiDisponibili) {
        this.postiDisponibili = postiDisponibili;
    }

    public void setVincoloLetteraCognome(String vincoloLetteraCognome) {
        this.vincoloLetteraCognome = vincoloLetteraCognome;
    }

    @Override
    public String toString() {
        return "Appello [codiceAppello=" + codiceAppello + ", codiceMateria=" + codiceMateria + ", dataOra=" + dataOra
                + ", aula=" + aula + ", postiDisponibili=" + postiDisponibili + ", vincoloLetteraCognome="
                + vincoloLetteraCognome + "]";
    }

    
}