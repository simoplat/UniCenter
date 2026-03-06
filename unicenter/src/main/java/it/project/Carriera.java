package it.project;
//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;

public class Carriera {
    private int matricola;
    private boolean stato;
    private Map <Materia,Integer> libretto;
    private double tasse;

    // Costruttore per creare una nuova carriera
    public Carriera(int matricola, double tasse) {
        this.matricola = matricola;
        this.tasse = tasse;
        this.stato = true;
        this.libretto = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Carriera [Matricola: " + matricola + "Stato: " + stato + ", Tasse: " + tasse + "€]";
    }

}
