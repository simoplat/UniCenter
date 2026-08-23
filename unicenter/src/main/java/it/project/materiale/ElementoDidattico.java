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
     *
     * @return id univoco
     */
    String getId();

    /**
     * Restituisce il nome dell'elemento (es. "Slide_01.pdf" o "Dispense").
     *
     * @return nome elemento
     */
    String getNome();

    /**
     * Imposta il nome dell'elemento.
     *
     * @param nome nuovo nome
     */
    void setNome(String nome);

    /**
     * Restituisce la descrizione o note dell'elemento.
     *
     * @return descrizione
     */
    String getDescrizione();

    /**
     * Restituisce il path relativo all'interno dello storage didattico.
     *
     * @return path relativo
     */
    String getPathRelativo();

    /**
     * Imposta il path relativo all'interno dello storage didattico.
     *
     * @param pathRelativo nuovo path relativo
     */
    void setPathRelativo(String pathRelativo);

    /**
     * Restituisce la data e ora di creazione/caricamento.
     *
     * @return data e ora di creazione
     */
    LocalDateTime getDataCreazione();

    /**
     * Restituisce la dimensione in byte dell'elemento (ricorsiva per le cartelle).
     *
     * @return dimensione totale in byte
     */
    long getDimensioneBytes();

    /**
     * Indica se l'elemento è un contenitore (Cartella) o una foglia.
     *
     * @return true se cartella, false se file foglia
     */
    boolean isCartella();

    /**
     * Restituisce l'ID del professore proprietario (o null se cartella radice/materia).
     *
     * @return id professore proprietario o null
     */
    String getOwnerProfessoreId();

    /**
     * Restituisce il codice della materia di appartenenza.
     *
     * @return codice materia
     */
    String getCodiceMateria();

    /**
     * Elenca gli elementi figli (per le cartelle) o restituisce una lista vuota/singleton per le foglie.
     *
     * @return lista elementi didattici figli
     */
    List<ElementoDidattico> elenca();

    /**
     * Restituisce l'anteprima polimorfica dell'elemento.
     *
     * @return AnteprimaRisultato
     */
    AnteprimaRisultato visualizza();
}
