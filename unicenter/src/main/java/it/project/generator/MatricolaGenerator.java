package it.project.generator;

/**
 * Pattern Utilizzati: Pure Fabrication & Singleton
 * Scopo: Generazione univoca e sequenziale dei numeri di matricola.
 */
public class MatricolaGenerator {
    private static MatricolaGenerator instance;
    private long currentSequence = 100000;

    private MatricolaGenerator() {}

    public static synchronized MatricolaGenerator getInstance() {
        if (instance == null) {
            instance = new MatricolaGenerator();
        }
        return instance;
    }

    public synchronized String generateMatricola() {
        return "M" + (++currentSequence);
    }
}