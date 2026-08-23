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
    private List<Materia> materiePreApprovate; // UC9: materie a scelta pre-approvate

    /**
     * Costruttore per creare un nuovo corso di laurea.
     *
     * @param id             identificativo univoco del corso di laurea (codice)
     * @param nome           denominazione del corso di laurea
     * @param tipologia      tipologia del corso (Triennale, Magistrale, Magistrale a Ciclo Unico, Master)
     * @param anniAccademici durata legale in anni accademici
     */
    public CorsoDiLaurea(String id, String nome, String tipologia, int anniAccademici) {
        this.id = id;
        this.nome = nome;
        this.tipologia = tipologia;
        this.anniAccademici = anniAccademici;
        this.materiePerAnno = new HashMap<>();
        this.obsoleto = false;
        this.finalizzato = false;
        this.materiePreApprovate = new ArrayList<>();
    }

    /**
     * Restituisce il nome del corso di laurea.
     *
     * @return nome corso
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce l'identificativo univoco del corso di laurea.
     *
     * @return id corso
     */
    public String getId() {
        return id;
    }

    /**
     * Imposta l'identificativo del corso di laurea.
     *
     * @param id nuovo id corso
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Imposta il nome del corso di laurea.
     *
     * @param nome nuovo nome del corso
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce la tipologia del corso di laurea.
     *
     * @return tipologia di laurea
     */
    public String getTipologia() {
        return tipologia;
    }

    /**
     * Imposta la tipologia del corso di laurea.
     *
     * @param tipologia nuova tipologia
     */
    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    /**
     * Restituisce il numero di anni accademici previsti per il corso.
     *
     * @return anni accademici
     */
    public int getAnniAccademici() {
        return anniAccademici;
    }

    /**
     * Imposta il numero di anni accademici del corso.
     *
     * @param anniAccademici durata in anni
     */
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

    /**
     * Verifica se il corso è stato finalizzato.
     *
     * @return true se finalizzato, false altrimenti
     */
    public boolean isFinalizzato() {
        return finalizzato;
    }

    /**
     * Restituisce la mappa completa anno e materie (vista non modificabile).
     *
     * @return mappa anno -&gt; lista materie
     */
    public Map<Integer, List<Materia>> getMateriePerAnno() {
        return Collections.unmodifiableMap(materiePerAnno);
    }

    /**
     * Restituisce le materie associate a un anno specifico.
     *
     * @param anno anno accademico richiesto
     * @return lista delle materie per l'anno specificato
     */
    public List<Materia> getMaterieByAnno(int anno) {
        return materiePerAnno.getOrDefault(anno, Collections.emptyList());
    }

    /**
     * Restituisce l'anno accademico (1-based) in cui è prevista la materia nel corso,
     * oppure 0 se non è presente nel manifesto.
     *
     * @param codiceMateria codice della materia da cercare
     * @return anno accademico di appartenenza, o 0 se non presente
     */
    public int getAnnoDellaMateria(String codiceMateria) {
        if (codiceMateria == null) return 0;
        for (Map.Entry<Integer, List<Materia>> entry : materiePerAnno.entrySet()) {
            for (Materia m : entry.getValue()) {
                if (m.getCodiceMateria().equalsIgnoreCase(codiceMateria)) {
                    return entry.getKey();
                }
            }
        }
        return 0;
    }

    /**
     * Metodo di utility: restituisce tutte le materie del corso come lista piatta (flat list),
     * aggregando le materie di tutti gli anni accademici (utilizzato ad esempio per il popolamento
     * automatico del piano di studi e viste riassuntive).
     *
     * @return lista piatta di tutte le materie del corso
     */
    public List<Materia> getMaterie() {
        List<Materia> tutteLeMaterie = new ArrayList<>();
        for (List<Materia> materieAnno : materiePerAnno.values()) {
            tutteLeMaterie.addAll(materieAnno);
        }
        return tutteLeMaterie;
    }

    /**
     * Rende il corso obsoleto: impedisce nuove iscrizioni senza eliminarlo dal
     * sistema.
     */
    public void rendiObsoleto() {
        this.obsoleto = true;
    }

    /**
     * Verifica se il corso di laurea è obsoleto.
     *
     * @return true se obsoleto, false se attivo
     */
    public boolean isObsoleto() {
        return obsoleto;
    }

    // =========================================================================
    // UC9 - INFORMATION EXPERT: Materie Pre-Approvate
    // =========================================================================

    /**
     * Information Expert: CorsoDiLaurea sa se una materia è pre-approvata.
     *
     * @param materia la materia da verificare
     * @return true se la materia è pre-approvata per questo corso
     */
    public boolean isPreApprovata(Materia materia) {
        if (materia == null) return false;
        return isPreApprovataByCodice(materia.getCodiceMateria());
    }

    /**
     * Verifica se una materia è pre-approvata dato il suo codice.
     *
     * @param codiceMateria il codice della materia
     * @return true se pre-approvata
     */
    public boolean isPreApprovataByCodice(String codiceMateria) {
        if (codiceMateria == null) return false;
        return materiePreApprovate.stream()
                .anyMatch(m -> m.getCodiceMateria().equalsIgnoreCase(codiceMateria));
    }

    /**
     * Aggiunge una materia all'elenco delle materie pre-approvate del corso.
     *
     * @param materia la materia da aggiungere
     */
    public void aggiungiMateriaPreApprovata(Materia materia) {
        if (materia == null) {
            throw new IllegalArgumentException("Materia non valida.");
        }
        if (!isPreApprovataByCodice(materia.getCodiceMateria())) {
            materiePreApprovate.add(materia);
        }
    }

    /**
     * Rimuove una materia dall'elenco delle materie pre-approvate.
     *
     * @param materia la materia da rimuovere
     */
    public void rimuoviMateriaPreApprovata(Materia materia) {
        if (materia != null) {
            materiePreApprovate.removeIf(m -> m.getCodiceMateria().equalsIgnoreCase(materia.getCodiceMateria()));
        }
    }

    /**
     * Restituisce la lista immutabile delle materie pre-approvate per il corso.
     *
     * @return lista immutabile di materie pre-approvate
     */
    public List<Materia> getMateriePreApprovate() {
        return Collections.unmodifiableList(materiePreApprovate);
    }

    /**
     * Verifica se tutte le materie a scelta passate sono pre-approvate per questo corso.
     *
     * @param materieAScelta la lista delle materie a scelta da verificare
     * @return true se tutte sono pre-approvate, false altrimenti
     */
    public boolean tuttePreApprovate(List<Materia> materieAScelta) {
        if (materieAScelta == null || materieAScelta.isEmpty()) {
            return true;
        }
        for (Materia m : materieAScelta) {
            if (!isPreApprovata(m)) {
                return false;
            }
        }
        return true;
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
