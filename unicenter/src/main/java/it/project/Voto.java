package it.project;

/**
 * Value Object immutabile che rappresenta la valutazione di un esame universitario.
 * Valida che il voto numerico sia compreso tra 18 e 30 e che la lode sia assegnabile solo al 30.
 */
public final class Voto {

    private final Integer valoreNumerico;
    private final boolean lode;

    private Voto(Integer valoreNumerico, boolean lode) {
        this.valoreNumerico = valoreNumerico;
        this.lode = lode;
    }

    /**
     * Factory method per la creazione di un'istanza valida di Voto.
     *
     * @param valore valore numerico compreso tra 18 e 30
     * @param lode   true se è presente la lode (consentita solo con valore 30)
     * @return nuova istanza di Voto
     * @throws IllegalArgumentException se il valore è &lt; 18 o &gt; 30, o se la lode è assegnata con voto diverso da 30
     */
    public static Voto numerico(int valore, boolean lode) {
        if (valore < 18 || valore > 30) {
            throw new IllegalArgumentException("Il voto numerico per un esame superato deve essere compreso tra 18 e 30.");
        }
        if (lode && valore != 30) {
            throw new IllegalArgumentException("La lode può essere assegnata solo con la votazione di 30.");
        }
        return new Voto(valore, lode);
    }

    /**
     * Restituisce il valore numerico del voto.
     *
     * @return valore intero (18-30)
     */
    public Integer getValoreNumerico() {
        return valoreNumerico;
    }

    /**
     * Indica se al voto è associata la lode.
     *
     * @return true se con lode, false altrimenti
     */
    public boolean haLode() {
        return lode;
    }
}