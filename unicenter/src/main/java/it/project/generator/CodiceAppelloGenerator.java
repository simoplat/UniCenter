package it.project.generator;

/**
 * Pattern: Pure Fabrication & Singleton:
 * Scopo: genera codici appello univoci di sistema per UC1.
 */
public class CodiceAppelloGenerator {
    private static CodiceAppelloGenerator instance;
    private long counter = 1;

    private CodiceAppelloGenerator() {}

    public static synchronized CodiceAppelloGenerator getInstance() {
        if (instance == null) {
            instance = new CodiceAppelloGenerator();
        }
        return instance;
    }

    public synchronized String generateCodice() {
        return "APP-" + String.format("%05d", counter++);
    }
}