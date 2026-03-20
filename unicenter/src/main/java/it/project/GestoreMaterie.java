package it.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class GestoreMaterie {
    private Map<String, Materia> materie;
    private Map<Integer, List<Integer>> materieDelProfessore = new HashMap<>();
    private Map<Integer, List<Integer>> professoriDellaMateria = new HashMap<>();

    public GestoreMaterie(Map<String, Materia> materie, Map<Integer, List<Integer>> materieDelProfessore,
        Map<Integer, List<Integer>> professoriDellaMateria) {
        this.materie = materie;
        this.materieDelProfessore = materieDelProfessore;
        this.professoriDellaMateria = professoriDellaMateria;
    }

    public void associaProfessoreAMateria(int idProfessore, int idMateria) {

        materieDelProfessore
                .computeIfAbsent(idProfessore, k -> new ArrayList<>())
                .add(idMateria);

        professoriDellaMateria
                .computeIfAbsent(idMateria, k -> new ArrayList<>())
                .add(idProfessore);
    }

    public List<Integer> trovaIdMaterieDiProfessore(int idProfessore) {
        return materieDelProfessore.getOrDefault(idProfessore, Collections.emptyList());
    }

    public List<Materia> trovaMaterieDiProfessore(int idProfessore) {
        List<Integer> idMaterie = trovaIdMaterieDiProfessore(idProfessore);
        List<Materia> risultato = new ArrayList<>();

        for (Integer id : idMaterie) {
            Materia m = materie.get(String.valueOf(id));
            if (m != null) {
                risultato.add(m);
            }
        }
        return risultato;
    }

    public boolean isProfessoreAbilitatoAMateria(int idProfessore, int idMateria) {
        List<Integer> materieProf = materieDelProfessore.get(idProfessore);
        return materieProf != null && materieProf.contains(idMateria);
    }
}