package it.project;

import java.util.ArrayList;
import java.util.List;

public class CorsoDiLaurea {
    private String id;
    private String nome;
    private List<Materia> materie;
    private int anniAccademici;

    public CorsoDiLaurea(String id, String nome, int anniAccademici) {
        this.id = id;
        this.nome = nome;
        this.anniAccademici = anniAccademici;
        this.materie = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Materia> getMaterie() {
        return materie;
    }

    public void setMaterie(List<Materia> materie) {
        this.materie = materie;
    }

    public void aggiungiMateria(Materia materia) {
        this.materie.add(materia);
    }

    public void rimuoviMateria(Materia materia) {
        this.materie.remove(materia);
    }
    
   
    
}
