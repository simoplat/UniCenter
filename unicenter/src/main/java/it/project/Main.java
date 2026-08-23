package it.project;

/**
 * Classe principale di avvio per l'applicazione UniCenter.
 * Avvia l'interfaccia utente testuale e il web server.
 */
public class Main {

    /**
     * Entry point dell'applicazione.
     *
     * @param args argomenti passati da riga di comando
     */
    public static void main(String[] args) {
        Unicenter unicenter = Unicenter.getInstance();
        unicenter.avvia();
    }
}