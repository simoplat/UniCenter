package it.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Information Expert (GRASP): CorsoDiLaurea conosce i propri attributi
 * (codice, denominazione, tipologia) e le materie appartenenti al manifesto
 * degli studi.
 * 
 * Creator (GRASP): CorsoDiLaurea associa le materie agli anni tramite
 * mappa (tra int anno e List di Materie).
 * 
 * Un corso appena creato è "non finalizzato": non ha materie associate
 * e non è visibile per l'immatricolazione. Una volta associatele materie
 * e finalizzato, il corso diventa immutabile (non modificabile, solo
 * rendibile obsoleto).
 */
public class CorsoDiLaurea {
    private String id;
    private String nome;
    private String tipologia; // Triennale, Magistrale, Magistrale a Ciclo Unico, Master
    private Map<Integer, List<Materia>> materiePerAnno;
    private int anniAccademici;
    private boolean obsoleto;
    private boolean finalizzato;

    public CorsoDiLaurea(String id, String nome, int anniAccademici) {
        this.id = id;
        this.nome = nome;
        this.anniAccademici = anniAccademici;
        this.materiePerAnno = new HashMap<>();
        this.obsoleto = false;
        this.finalizzato = false;
    }

    public CorsoDiLaurea(String id, String nome, String tipologia, int anniAccademici) {
        this.id = id;
        this.nome = nome;
        this.tipologia = tipologia;
        this.anniAccademici = anniAccademici;
        this.materiePerAnno = new HashMap<>();
        this.obsoleto = false;
        this.finalizzato = false;
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

    // =========================================================================
    // GESTIONE MATERIE PER ANNO (Creator GRASP)
    // =========================================================================

    /**
     * Associa una materia a un anno accademico del corso.
     * Il corso non deve essere già finalizzato e l'anno deve essere
     * coerente con il numero di anni accademici del corso.
     *
     * @param anno    l'anno accademico (1-based, da 1 a anniAccademici)
     * @param materia la materia da associare
     * @throws IllegalStateException    se il corso è già finalizzato
     * @throws IllegalArgumentException se l'anno è fuori range
     */
    public void aggiungiMateriaAdAnno(int anno, Materia materia) {
        if (finalizzato) {
            throw new IllegalStateException(
                    "Impossibile modificare il corso '" + nome + "': è già finalizzato.");
        }
        if (anno < 1 || anno > anniAccademici) {
            throw new IllegalArgumentException(
                    "Anno non valido: " + anno + ". Deve essere compreso tra 1 e " + anniAccademici + ".");
        }
        materiePerAnno.computeIfAbsent(anno, k -> new ArrayList<>()).add(materia);
    }

    /**
     * Finalizza il corso: dopo questa operazione non è più possibile
     * aggiungere o rimuovere materie. Il corso diventa visibile per
     * l'immatricolazione.
     *
     * @throws IllegalStateException se il corso è già finalizzato o non ha materie
     */
    public void finalizza() {
        if (finalizzato) {
            throw new IllegalStateException("Il corso '" + nome + "' è già finalizzato.");
        }
        if (materiePerAnno.isEmpty()) {
            throw new IllegalStateException(
                    "Impossibile finalizzare il corso '" + nome + "': nessuna materia associata.");
        }
        this.finalizzato = true;
    }

    public boolean isFinalizzato() {
        return finalizzato;
    }

    /**
     * Restituisce la mappa completa anno → materie (vista non modificabile).
     */
    public Map<Integer, List<Materia>> getMateriePerAnno() {
        return Collections.unmodifiableMap(materiePerAnno);
    }

    /**
     * Restituisce le materie associate a un anno specifico.
     */
    public List<Materia> getMaterieByAnno(int anno) {
        return materiePerAnno.getOrDefault(anno, Collections.emptyList());
    }

    /**
     * Restituisce tutte le materie del corso (flat list, senza distinzione per anno).
     * Mantenuto per retrocompatibilità con piano di studi e altre parti del codice.
     */
    public List<Materia> getMaterie() {
        List<Materia> tutteLeMaterie = new ArrayList<>();
        for (List<Materia> materieAnno : materiePerAnno.values()) {
            tutteLeMaterie.addAll(materieAnno);
        }
        return tutteLeMaterie;
    }

    /**
     * Metodo legacy per compatibilità con il popolamento dati di test.
     * Aggiunge una materia senza specificare l'anno (usa anno 1 come default).
     */
    public void aggiungiMateria(Materia materia) {
        if (finalizzato) {
            throw new IllegalStateException(
                    "Impossibile modificare il corso '" + nome + "': è già finalizzato.");
        }
        materiePerAnno.computeIfAbsent(1, k -> new ArrayList<>()).add(materia);
    }

    /**
     * Rende il corso obsoleto: impedisce nuove iscrizioni senza eliminarlo dal
     * sistema.
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
                + ", stato=" + (obsoleto ? "OBSOLETO" : "ATTIVO")
                + ", finalizzato=" + (finalizzato ? "SI" : "NO") + "]";
    }
}
