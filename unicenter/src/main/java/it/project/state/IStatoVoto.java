package it.project.state;

import it.project.EsameSostenuto;

/**
 * State Pattern (GoF Comportamentale) – Interfaccia per gli stati del voto.
 * Ogni stato concreto decide quali transizioni sono lecite.
 */
public interface IStatoVoto {

    /**
     * Tenta la transizione verso lo stato "Approvato".
     *
     * @param esame istanza dell'esame sostenuto
     * @throws IllegalStateException se la transizione non è consentita dallo stato corrente
     */
    void accetta(EsameSostenuto esame);

    /**
     * Tenta la transizione verso lo stato "Rifiutato".
     *
     * @param esame istanza dell'esame sostenuto
     * @throws IllegalStateException se la transizione non è consentita dallo stato corrente
     */
    void rifiuta(EsameSostenuto esame);

    /**
     * Restituisce il nome leggibile dello stato.
     *
     * @return il nome leggibile dello stato (es. "In attesa di conferma")
     */
    String getNome();
}
