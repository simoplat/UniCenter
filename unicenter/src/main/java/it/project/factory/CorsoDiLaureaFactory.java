package it.project.factory;

import it.project.CorsoDiLaurea;
import it.project.generator.CodiceCorsoGenerator;

/**
 * Factory Method (GoF Creazionale):
 * Incapsula la logica di creazione di un CorsoDiLaurea,
 * applicando le regole di convalida dei dati iniziali prima dell'istanziazione.
 */
public class CorsoDiLaureaFactory {

    private static final String[] TIPOLOGIE_VALIDE = {
        "Triennale", "Magistrale", "Magistrale a Ciclo Unico", "Master"
    };

    /**
     * Crea un nuovo CorsoDiLaurea con validazione dei dati e generazione del codice univoco.
     *
     * @param nome            denominazione del corso (es. "Ingegneria Informatica")
     * @param tipologia       tipo di corso: Triennale, Magistrale, Magistrale a Ciclo Unico, Master
     * @param anniAccademici  durata in anni (deve essere coerente con la tipologia)
     * @return CorsoDiLaurea completamente inizializzato
     * @throws IllegalArgumentException se i dati non superano la validazione
     */
    public static CorsoDiLaurea creaCorsoDiLaurea(String nome, String tipologia, int anniAccademici) {
        // Validazione nome
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del corso di laurea è obbligatorio.");
        }

        // Validazione tipologia
        if (tipologia == null || !isTipologiaValida(tipologia)) {
            throw new IllegalArgumentException(
                "Tipologia non valida. Valori ammessi: Triennale, Magistrale, Magistrale a Ciclo Unico, Master.");
        }

        // Validazione coerenza tipologia-anni
        int anniAttesi = getAnniPerTipologia(tipologia);
        if (anniAccademici != anniAttesi) {
            throw new IllegalArgumentException(
                "Il numero di anni accademici (" + anniAccademici + ") non è coerente con la tipologia '"
                + tipologia + "' (attesi: " + anniAttesi + " anni).");
        }

        // Generazione codice univoco (Regola di Dominio 3)
        String codice = CodiceCorsoGenerator.getInstance().generateCodice();

        return new CorsoDiLaurea(codice, nome.trim(), tipologia, anniAccademici);
    }

    /**
     * Verifica se la tipologia è tra quelle valide.
     */
    private static boolean isTipologiaValida(String tipologia) {
        for (String t : TIPOLOGIE_VALIDE) {
            if (t.equalsIgnoreCase(tipologia)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Restituisce il numero di anni attesi per ciascuna tipologia.
     *
     * @param tipologia denominazione della tipologia
     * @return numero di anni accademici attesi
     * @throws IllegalArgumentException se la tipologia è sconosciuta
     */
    public static int getAnniPerTipologia(String tipologia) {
        switch (tipologia.toLowerCase()) {
            case "triennale":
                return 3;
            case "magistrale":
                return 2;
            case "magistrale a ciclo unico":
                return 5;
            case "master":
                return 2;
            default:
                throw new IllegalArgumentException("Tipologia sconosciuta: " + tipologia);
        }
    }

    /**
     * Restituisce l'array delle tipologie valide supportate.
     *
     * @return array di stringhe delle tipologie valide
     */
    public static String[] getTipologieValide() {
        return TIPOLOGIE_VALIDE.clone();
    }
}
