package it.project;

import java.util.ArrayList;
import java.util.List;

public class PianoDiStudi {
    private String stato; // es. "APPROVATO", "IN_ATTESA"
    private List<String> idMaterie;

    public PianoDiStudi() {
        this.stato = "APPROVATO";
        this.idMaterie = new ArrayList<>();
    }

    public void aggiungiMateria(String codiceMateria) {
        this.idMaterie.add(codiceMateria);
    }

    public boolean contieneMateria(String codiceMateria) {
        return idMaterie.contains(codiceMateria);
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public List<String> getIdMaterie() {
        return idMaterie;
    }
}