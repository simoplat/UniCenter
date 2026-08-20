package it.project.generator;

/**
 * Pattern Utilizzati: Pure Fabrication & Singleton
 * Scopo: Generazione univoca e sequenziale degli ID dei verbali d'esame (UC3).
 * Formato: VRB-XXXXX (es. VRB-00001)
 */
public class IdVerbaleGenerator {
    private static IdVerbaleGenerator instance;
    private long counter = 1;

    private IdVerbaleGenerator() {}

    public static synchronized IdVerbaleGenerator getInstance() {
        if (instance == null) {
            instance = new IdVerbaleGenerator();
        }
        return instance;
    }

    public synchronized String generateId() {
        return "VRB-" + String.format("%05d", counter++);
    }
}
