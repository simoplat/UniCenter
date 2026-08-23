package it.project.view;

import java.util.List;
import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.Studente;

/**
 * Interfaccia per la separazione delle responsabilità della View (Console o Web).
 */
public interface UniCenterView {
    /**
     * Mostra un messaggio informativo all'utente.
     *
     * @param messaggio testo del messaggio
     */
    void mostraMessaggio(String messaggio);

    /**
     * Mostra un messaggio di errore all'utente.
     *
     * @param errore testo dell'errore
     */
    void mostraErrore(String errore);

    /**
     * Stampa o visualizza la lista degli appelli d'esame.
     *
     * @param appelli lista appelli
     */
    void stampaAppelli(List<Appello> appelli);

    /**
     * Stampa o visualizza la lista delle materie.
     *
     * @param materie lista materie
     */
    void stampaMaterie(List<Materia> materie);

    /**
     * Stampa o visualizza la lista degli studenti.
     *
     * @param studenti lista studenti
     */
    void stampaStudenti(List<Studente> studenti);

    /**
     * Stampa o visualizza la lista dei corsi di laurea.
     *
     * @param corsi lista corsi di laurea
     */
    void stampaCorsiDiLaurea(List<CorsoDiLaurea> corsi);

    /**
     * Stampa o visualizza la lista degli esiti d'esame.
     *
     * @param esiti lista esami sostenuti
     */
    void stampaEsiti(List<EsameSostenuto> esiti);

    /**
     * Stampa o visualizza l'albero gerarchico dei materiali didattici.
     *
     * @param radice cartella radice dei materiali
     */
    void stampaAlberoMateriali(it.project.materiale.Cartella radice);
}

