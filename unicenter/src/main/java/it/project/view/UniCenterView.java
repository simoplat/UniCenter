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
    void mostraMessaggio(String messaggio);
    void mostraErrore(String errore);
    void stampaAppelli(List<Appello> appelli);
    void stampaMaterie(List<Materia> materie);
    void stampaStudenti(List<Studente> studenti);
    void stampaCorsiDiLaurea(List<CorsoDiLaurea> corsi);
    void stampaEsiti(List<EsameSostenuto> esiti);
}
