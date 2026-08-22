package it.project.materiale;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Component Interface per il Pattern GoF Composite.
 * Rappresenta uniformemente sia contenitori (Cartelle) che foglie (Materiali Didattici).
 */
public interface ElementoDidattico {

    /**
     * Restituisce l'ID univoco dell'elemento.
     */
    String getId();

    /**
     * Restituisce il nome dell'elemento (es. "Slide_01.pdf" o "Dispense").
     */
    String getNome();

    /**
     * Imposta il nome dell'elemento.
     */
    void setNome(String nome);

    /**
     * Restituisce la descrizione o note dell'elemento.
     */
    String getDescrizione();

    /**
     * Restituisce il path relativo all'interno dello storage didattico.
     */
    String getPathRelativo();

    /**
     * Imposta il path relativo all'interno dello storage didattico.
     */
    void setPathRelativo(String pathRelativo);

    /**
     * Restituisce la data e ora di creazione/caricamento.
     */
    LocalDateTime getDataCreazione();

    /**
     * Restituisce la dimensione in byte dell'elemento (ricorsiva per le cartelle).
     */
    long getDimensioneBytes();

    /**
     * Indica se l'elemento è un contenitore (Cartella) o una foglia.
     */
    boolean isCartella();

    /**
     * Restituisce l'ID del professore proprietario (o null se cartella radice/materia).
     */
    String getOwnerProfessoreId();

    /**
     * Restituisce il codice della materia di appartenenza.
     */
    String getCodiceMateria();

    /**
     * Elenca gli elementi figli (per le cartelle) o restituisce una lista vuota/singleton per le foglie.
     */
    List<ElementoDidattico> elenca();

    /**
     * Restituisce l'anteprima polimorfica dell'elemento.
     */
    AnteprimaRisultato visualizza();
}
