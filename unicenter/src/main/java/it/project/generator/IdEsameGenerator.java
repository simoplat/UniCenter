package it.project.generator;

/**
 * Pattern Utilizzati: Pure Fabrication & Singleton
 * Scopo: Generazione univoca e sequenziale degli ID degli esami sostenuti (UC3).
 * Formato: ESM-XXXXX (es. ESM-00001)
 */
public class IdEsameGenerator {
    private static IdEsameGenerator instance;
    private long counter = 1;

    private IdEsameGenerator() {}

    public static synchronized IdEsameGenerator getInstance() {
        if (instance == null) {
            instance = new IdEsameGenerator();
        }
        return instance;
    }

    public synchronized String generateId() {
        return "ESM-" + String.format("%05d", counter++);
    }
}
