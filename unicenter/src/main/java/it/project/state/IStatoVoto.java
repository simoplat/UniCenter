package it.project.state;

import it.project.EsameSostenuto;

/**
 * State Pattern (GoF Comportamentale) – Interfaccia per gli stati del voto.
 * Ogni stato concreto decide quali transizioni sono lecite.
 */
public interface IStatoVoto {

    /**
     * Tenta la transizione verso lo stato "Approvato".
     * @throws IllegalStateException se la transizione non è consentita dallo stato corrente
     */
    void accetta(EsameSostenuto esame);

    /**
     * Tenta la transizione verso lo stato "Rifiutato".
     * @throws IllegalStateException se la transizione non è consentita dallo stato corrente
     */
    void rifiuta(EsameSostenuto esame);

    /**
     * @return il nome leggibile dello stato (es. "In attesa di conferma")
     */
    String getNome();
}
