package it.project.strategy;

public class CalcoloTasseStandardStrategy implements ICalcoloTasseStrategy {
    @Override
    public double calcolaTasse(double tassaBaseCorso, boolean isFuoriCorso) {
        double totale = tassaBaseCorso;
        if (isFuoriCorso) {
            totale += 300.00; // Maggiorazione standard per studenti fuori corso
        }
        return totale;
    }
}