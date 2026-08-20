package it.project.observer;

import it.project.Notifica;

/**
 * Observer (GoF Comportamentale) unificato per la ricezione di notifiche di sistema.
 * Implementato da Studente (e potenzialmente da altri attori o dispositivi) per ricevere
 * avvisi e comunicazioni da Subject/Observable quali Appello, Materia, ecc.
 */
public interface ObserverNotifica {

    /**
     * Riceve una notifica inviata da un Subject (es. Materia, Appello).
     * 
     * @param notifica la notifica contenente oggetto, messaggio e timestamp
     */
    void riceviNotifica(Notifica notifica);
}
