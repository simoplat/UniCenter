package it.project;

public final class Voto {

    private final Integer valoreNumerico;
    private final boolean lode;

    private Voto(Integer valoreNumerico, boolean lode) {
        this.valoreNumerico = valoreNumerico;
        this.lode = lode;
    }

    public static Voto numerico(int valore, boolean lode) {
        if (valore < 18 || valore > 30) {
            throw new IllegalArgumentException("Il voto numerico per un esame superato deve essere compreso tra 18 e 30.");
        }
        if (lode && valore != 30) {
            throw new IllegalArgumentException("La lode può essere assegnata solo con la votazione di 30.");
        }
        return new Voto(valore, lode);
    }


    public boolean haLode() {
        return lode;
    }
}