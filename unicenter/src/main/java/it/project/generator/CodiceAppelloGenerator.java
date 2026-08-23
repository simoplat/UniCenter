package it.project.generator;

/**
 * Pattern: Pure Fabrication e Singleton.
 * Scopo: genera codici appello univoci di sistema nel formato APP-XXXXX.
 */
public class CodiceAppelloGenerator {
    private static CodiceAppelloGenerator instance;
    private long counter = 1;

    private CodiceAppelloGenerator() {}

    /**
     * Restituisce l'istanza Singleton del generatore codici appello.
     *
     * @return istanza condivisa di CodiceAppelloGenerator
     */
    public static synchronized CodiceAppelloGenerator getInstance() {
        if (instance == null) {
            instance = new CodiceAppelloGenerator();
        }
        return instance;
    }

    /**
     * Genera in modo incrementale e thread-safe un nuovo codice appello univoco.
     *
     * @return stringa con il codice generato (es. APP-00001)
     */
    public synchronized String generateCodice() {
        return "APP-" + String.format("%05d", counter++);
    }
}