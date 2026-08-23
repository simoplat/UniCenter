package it.project.strategy;

import it.project.PianoDiStudi;

/**
 * Strategy Pattern (GoF Comportamentale) – Politica di approvazione del Piano di Studi.
 * Protected Variations: se le regole di approvazione cambiano, si aggiunge
 * una nuova strategia senza modificare il Controller.
 */
public interface PoliticaApprovazione {
    /**
     * Applica la politica di approvazione al piano di studi fornito.
     *
     * @param piano il piano di studi su cui applicare la strategia
     */
    void applica(PianoDiStudi piano);
}
