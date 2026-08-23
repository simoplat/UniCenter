package it.project.strategy;

/**
 * Strategy Pattern per il calcolo dell'importo tasse universitarie.
 */
public interface ICalcoloTasseStrategy {

    /**
     * Calcola l'importo totale delle tasse universitarie dovute.
     *
     * @param tassaBaseCorso importo base del corso di laurea
     * @param isFuoriCorso   flag che indica se lo studente è fuori corso
     * @return importo totale tasse
     */
    double calcolaTasse(double tassaBaseCorso, boolean isFuoriCorso);
}