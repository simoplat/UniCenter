package it.project.generator;

/**
 * Pattern Utilizzati: Pure Fabrication e Singleton.
 * Scopo: Generazione univoca e sequenziale dei numeri di matricola studente.
 */
public class MatricolaGenerator {
    private static MatricolaGenerator instance;
    private long currentSequence = 100000;

    private MatricolaGenerator() {}

    /**
     * Restituisce l'istanza Singleton del generatore matricole.
     *
     * @return istanza condivisa di MatricolaGenerator
     */
    public static synchronized MatricolaGenerator getInstance() {
        if (instance == null) {
            instance = new MatricolaGenerator();
        }
        return instance;
    }

    /**
     * Genera in modo incrementale e thread-safe un nuovo numero di matricola.
     *
     * @return stringa matricola (es. M100001)
     */
    public synchronized String generateMatricola() {
        return "M" + (++currentSequence);
    }
}