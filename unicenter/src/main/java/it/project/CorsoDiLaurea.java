package it.project;

import java.util.ArrayList;
import java.util.List;

/**
 * Information Expert (GRASP): CorsoDiLaurea conosce i propri attributi
 * (codice, denominazione, tipologia) e le materie appartenenti al manifesto degli studi.
 */
public class CorsoDiLaurea {
    private String id;
    private String nome;
    private String tipologia; // Triennale, Magistrale, Magistrale a Ciclo Unico, Master
    private List<Materia> materie;
    private int anniAccademici;
    private boolean obsoleto;

    public CorsoDiLaurea(String id, String nome, int anniAccademici) {
        this.id = id;
        this.nome = nome;
        this.anniAccademici = anniAccademici;
        this.materie = new ArrayList<>();
        this.obsoleto = false;
    }

    public CorsoDiLaurea(String id, String nome, String tipologia, int anniAccademici) {
        this.id = id;
        this.nome = nome;
        this.tipologia = tipologia;
        this.anniAccademici = anniAccademici;
        this.materie = new ArrayList<>();
        this.obsoleto = false;
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

    public String getTipologia() {
        return tipologia;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public int getAnniAccademici() {
        return anniAccademici;
    }

    public void setAnniAccademici(int anniAccademici) {
        this.anniAccademici = anniAccademici;
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

    /**
     * Rende il corso obsoleto: impedisce nuove iscrizioni senza eliminarlo dal sistema.
     */
    public void rendiObsoleto() {
        this.obsoleto = true;
    }

    public boolean isObsoleto() {
        return obsoleto;
    }

    @Override
    public String toString() {
        return "CorsoDiLaurea [codice=" + id + ", nome=" + nome
                + ", tipologia=" + (tipologia != null ? tipologia : "N/D")
                + ", anni=" + anniAccademici
                + ", stato=" + (obsoleto ? "OBSOLETO" : "ATTIVO") + "]";
    }
}
