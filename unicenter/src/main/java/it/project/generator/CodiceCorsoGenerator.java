package it.project.generator;

import java.time.Year;

/**
 * Pattern Utilizzati: Pure Fabrication & Singleton
 * Scopo: Generazione univoca e thread-safe dei codici dei Corsi di Laurea.
 * Formato: CDL-{ANNO}-{SEQUENZA} (es. CDL-2026-001)
 * 
 * Regola di Dominio 3: Il sistema genera un codice identificativo univoco.
 */
public class CodiceCorsoGenerator {
    private static CodiceCorsoGenerator instance;
    private long counter = 1;

    private CodiceCorsoGenerator() {}

    public static synchronized CodiceCorsoGenerator getInstance() {
        if (instance == null) {
            instance = new CodiceCorsoGenerator();
        }
        return instance;
    }

    public synchronized String generateCodice() {
        int anno = Year.now().getValue();
        return "CDL-" + anno + "-" + String.format("%03d", counter++);
    }
}
