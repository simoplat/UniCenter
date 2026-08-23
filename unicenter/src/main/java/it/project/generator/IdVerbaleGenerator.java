package it.project.generator;

/**
 * Pattern Utilizzati: Pure Fabrication e Singleton.
 * Scopo: Generazione univoca e sequenziale degli ID dei verbali d'esame (UC3).
 * Formato: VRB-XXXXX (es. VRB-00001).
 */
public class IdVerbaleGenerator {
    private static IdVerbaleGenerator instance;
    private long counter = 1;

    private IdVerbaleGenerator() {}

    /**
     * Restituisce l'istanza Singleton del generatore ID verbale.
     *
     * @return istanza condivisa di IdVerbaleGenerator
     */
    public static synchronized IdVerbaleGenerator getInstance() {
        if (instance == null) {
            instance = new IdVerbaleGenerator();
        }
        return instance;
    }

    /**
     * Genera in modo incrementale e thread-safe un nuovo identificativo verbale.
     *
     * @return stringa ID verbale (es. VRB-00001)
     */
    public synchronized String generateId() {
        return "VRB-" + String.format("%05d", counter++);
    }
}
