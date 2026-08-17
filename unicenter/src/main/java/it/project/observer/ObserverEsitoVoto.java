package it.project.observer;

import it.project.EsameSostenuto;

/**
 * Observer (GoF Comportamentale) – Interfaccia per gli osservatori del cambio
 * di stato del voto.
 */
public interface ObserverEsitoVoto {

    /**
     * Invocato quando lo stato di un esame sostenuto cambia.
     * 
     * @param esame      l'esame il cui stato è cambiato
     * @param nuovoStato il nome del nuovo stato
     */
    void aggiornamento(EsameSostenuto esame, String nuovoStato);
}
