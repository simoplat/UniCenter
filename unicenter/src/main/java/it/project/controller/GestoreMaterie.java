package it.project.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.project.Materia;

import java.util.Collections;

public class GestoreMaterie {
    private Map<String, Materia> materie;
    private Map<String, List<String>> materieDelProfessore = new HashMap<>();
    private Map<String, List<String>> professoriDellaMateria = new HashMap<>();

    public GestoreMaterie() {
        this.materie = new HashMap<>();
        this.materieDelProfessore = new HashMap<>();
        this.professoriDellaMateria = new HashMap<>();
    }

    public void associaProfessoreAMateria(String idProfessore, String idMateria) {

        materieDelProfessore
                .computeIfAbsent(idProfessore, k -> new ArrayList<>())
                .add(idMateria);

        professoriDellaMateria
                .computeIfAbsent(idMateria, k -> new ArrayList<>())
                .add(idProfessore);
    }

    public List<String> trovaIdMaterieDiProfessore(String idProfessore) {
        return materieDelProfessore.getOrDefault(idProfessore, Collections.emptyList());
    }

    public List<Materia> trovaMaterieDiProfessore(String idProfessore) {
        List<String> idMaterie = trovaIdMaterieDiProfessore(idProfessore);
        List<Materia> risultato = new ArrayList<>();

        for (String idMateria : idMaterie) {
            Materia materia = materie.get(idMateria);
            if (materia != null) {
                risultato.add(materia);
            }
        }
        return risultato;
    }

    public boolean isProfessoreAbilitatoAMateria(String idProfessore, String idMateria) {
        List<String> materieProf = materieDelProfessore.get(idProfessore);
        return materieProf != null && materieProf.contains(idMateria);
    }
}