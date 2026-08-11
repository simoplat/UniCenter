package it.project.controller;

import java.util.ArrayList;
import java.util.List;


import it.project.CorsoDiLaurea;
import it.project.Unicenter;

public class CorsoDiLaureaController {
    private List<CorsoDiLaurea> corsiDiLaurea;
    Unicenter unicenter;


    public CorsoDiLaureaController(Unicenter unicenter) {
        this.unicenter = unicenter;
        this.corsiDiLaurea = new ArrayList<>();
    }

    public List<CorsoDiLaurea> getCorsiDiLaurea() {
        return corsiDiLaurea;
    }

    public void setCorsiDiLaurea(List<CorsoDiLaurea> corsiDiLaurea) {
        this.corsiDiLaurea = corsiDiLaurea;
    }

    public CorsoDiLaurea trovaCorsoDiLaureaByNome(String nome) {
        for (CorsoDiLaurea corso : corsiDiLaurea) {
            if (corso.getNome().equalsIgnoreCase(nome)) {
                return corso;
            }
        }
        return null; // Corso di laurea non trovato
    }

    public void addCorsoDiLaurea(CorsoDiLaurea corso) {
        corsiDiLaurea.add(corso);
    }

    
}
