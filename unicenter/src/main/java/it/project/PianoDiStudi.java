package it.project;

import java.util.ArrayList;
import java.util.List;

public class PianoDiStudi {
    private String stato; // es. "APPROVATO", "IN_ATTESA"
    private List<String> codiciMaterie;

    public PianoDiStudi() {
        this.stato = "APPROVATO";
        this.codiciMaterie = new ArrayList<>();
    }

    public void aggiungiMateria(String codiceMateria) {
        this.codiciMaterie.add(codiceMateria);
    }

    public boolean contieneMateria(String codiceMateria) {
        return codiciMaterie.contains(codiceMateria);
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }
}