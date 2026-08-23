package it.project.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.project.Materia;

import java.util.Collections;

/**
 * Controller (GRASP / Facade Controller) per UC5 - Gestione Materie.
 * Gestisce le interazioni dell'interfaccia amministrativa per le materie:
 * creazione, ricerca e associazione materie-professori.
 */
public class GestoreMaterieController {
    private Map<String, Materia> materie;
    private Map<String, List<String>> materieDelProfessore = new HashMap<>();
    private Map<String, List<String>> professoriDellaMateria = new HashMap<>();
    private int contatoreCodiceMateria = 0;

    /**
     * Costruttore di default. Inizializza le mappe in memoria delle materie e associazioni docenti.
     */
    public GestoreMaterieController() {
        this.materie = new HashMap<>();
        this.materieDelProfessore = new HashMap<>();
        this.professoriDellaMateria = new HashMap<>();
    }

    // =========================================================================
    // UC5 - CREAZIONE MATERIA (Controller GRASP)
    // =========================================================================

    /**
     * Crea una nuova materia con nome e CFU. Il codice viene generato
     * automaticamente in formato MAT-XXXXX.
     *
     * @param nome il nome della materia
     * @param cfu  il numero di crediti formativi
     * @return la Materia appena creata
     * @throws IllegalArgumentException se il nome è vuoto o i CFU non sono validi
     */
    public Materia creaMateria(String nome, int cfu) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della materia è obbligatorio.");
        }
        if (cfu <= 0) {
            throw new IllegalArgumentException("I CFU devono essere un numero positivo.");
        }

        // Verifica duplicati per nome
        for (Materia m : materie.values()) {
            if (m.getNome().equalsIgnoreCase(nome.trim())) {
                throw new IllegalArgumentException(
                        "Esiste già una materia con il nome '" + nome + "' (codice: " + m.getCodiceMateria() + ").");
            }
        }

        String codice = generaCodiceMateria();
        Materia nuovaMateria = new Materia(codice, nome.trim(), cfu);
        materie.put(codice, nuovaMateria);
        return nuovaMateria;
    }

    /**
     * Genera un codice materia univoco nel formato MAT-XXXXX.
     */
    private String generaCodiceMateria() {
        contatoreCodiceMateria++;
        return String.format("MAT-%05d", contatoreCodiceMateria);
    }

    /**
     * Restituisce la lista di tutte le materie create nel sistema.
     *
     * @return lista di tutte le materie
     */
    public List<Materia> getTutteLeMaterie() {
        return new ArrayList<>(materie.values());
    }

    // =========================================================================
    // METODI ESISTENTI
    // =========================================================================

    /**
     * Aggiunge una materia alla mappa interna.
     *
     * @param materia materia da aggiungere
     */
    public void addMateria(Materia materia) {
        materie.put(materia.getCodiceMateria(), materia);
    }

    /**
     * Associa un professore all'insegnamento di una materia.
     *
     * @param idProfessore id del professore
     * @param idMateria    codice della materia
     */
    public void associaProfessoreAMateria(String idProfessore, String idMateria) {

        if (isProfessoreAbilitatoAMateria(idProfessore, idMateria)) {
            return;
        }
        materieDelProfessore
                .computeIfAbsent(idProfessore, k -> new ArrayList<>())
                .add(idMateria);

        professoriDellaMateria
                .computeIfAbsent(idMateria, k -> new ArrayList<>())
                .add(idProfessore);
    }

    /**
     * Restituisce la lista degli ID delle materie assegnate a un professore.
     *
     * @param idProfessore id del professore
     * @return lista codici materia
     */
    public List<String> trovaIdMaterieDiProfessore(String idProfessore) {
        return materieDelProfessore.getOrDefault(idProfessore, Collections.emptyList());
    }

    /**
     * Restituisce la lista degli oggetti {@link Materia} assegnati a un professore.
     *
     * @param idProfessore id del professore
     * @return lista materie
     */
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

    /**
     * Verifica se un professore è abilitato alla gestione di una materia.
     *
     * @param idProfessore id del professore
     * @param idMateria    codice della materia
     * @return true se abilitato
     */
    public boolean isProfessoreAbilitatoAMateria(String idProfessore, String idMateria) {
        List<String> materieProf = materieDelProfessore.get(idProfessore);
        return materieProf != null && materieProf.contains(idMateria);
    }

    /**
     * Cerca una materia tramite il suo codice univoco.
     *
     * @param codiceMateria codice materia
     * @return Materia trovata o null
     */
    public Materia trovaMaterieByCodice(String codiceMateria) {
        return materie.get(codiceMateria);
    }

    /**
     * Restituisce la lista degli ID dei professori associati a una materia.
     *
     * @param codiceMateria codice della materia
     * @return lista id professori associati
     */
    public List<String> trovaProfessoriDellaMateria(String codiceMateria) {
        return professoriDellaMateria.getOrDefault(codiceMateria, Collections.emptyList());
    }
}