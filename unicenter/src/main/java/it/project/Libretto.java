package it.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Information Expert (GRASP): Libretto è l'esperto dell'informazione che
 * possiede la lista degli esami superati (EsameSostenuto con stato "Approvato")
 * e calcola la media ponderata.
 *
 * Unificazione: questa classe sostituisce la versione precedente basata su Map<Materia, Voto>
 * e gestisce direttamente gli EsameSostenuto.
 */
public class Libretto {

    private final List<EsameSostenuto> esamiSuperati;

    public Libretto() {
        this.esamiSuperati = new ArrayList<>();
    }

    /**
     * Registra un esame superato nel libretto.
     * Solo gli esami con stato "Approvato" possono essere registrati.
     *
     * @param esame l'esame sostenuto da registrare
     * @throws IllegalStateException se l'esame non è in stato "Approvato"
     * @throws IllegalArgumentException se la materia è già presente nel libretto
     */
    public void registraEsame(EsameSostenuto esame) {
        if (!"Approvato".equals(esame.getNomeStato())) {
            throw new IllegalStateException(
                    "Solo gli esami approvati possono essere registrati nel libretto. Stato attuale: "
                            + esame.getNomeStato());
        }
        if (isEsameSuperato(esame.getCodiceMateria())) {
            throw new IllegalArgumentException(
                    "Esame già registrato nel libretto per la materia: " + esame.getCodiceMateria());
        }
        esamiSuperati.add(esame);
    }

    /**
     * Verifica se una materia è già stata superata e registrata nel libretto.
     *
     * @param codiceMateria il codice della materia da verificare
     * @return true se la materia è già presente nel libretto
     */
    public boolean isEsameSuperato(String codiceMateria) {
        return getEsameSuperato(codiceMateria) != null;
    }

    /**
     * Restituisce l'esame superato per una specifica materia, oppure null se non ancora superato.
     *
     * @param codiceMateria il codice della materia
     * @return EsameSostenuto corrispondente o null
     */
    public EsameSostenuto getEsameSuperato(String codiceMateria) {
        if (codiceMateria == null) return null;
        for (EsameSostenuto e : esamiSuperati) {
            if (e.getCodiceMateria().equalsIgnoreCase(codiceMateria)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Calcola la media ponderata dei voti registrati nel libretto.
     * Formula: Σ(voto × CFU) / Σ(CFU)
     *
     * @return la media ponderata, oppure 0.0 se il libretto è vuoto
     */
    public double getMediaPonderata() {
        if (esamiSuperati.isEmpty()) {
            return 0.0;
        }

        double sommaPesata = 0.0;
        int sommaCfu = 0;

        for (EsameSostenuto esame : esamiSuperati) {
            sommaPesata += esame.getVotoNumerico() * esame.getCfu();
            sommaCfu += esame.getCfu();
        }

        if (sommaCfu == 0) {
            return 0.0;
        }

        return sommaPesata / sommaCfu;
    }

    /**
     * @return la lista immutabile degli esami superati
     */
    public List<EsameSostenuto> getEsamiSuperati() {
        return Collections.unmodifiableList(esamiSuperati);
    }

    /**
     * @return il numero totale di CFU acquisiti
     */
    public int getTotaleCfu() {
        int totale = 0;
        for (EsameSostenuto esame : esamiSuperati) {
            totale += esame.getCfu();
        }
        return totale;
    }

    /**
     * @return il numero di esami superati
     */
    public int getNumeroEsamiSuperati() {
        return esamiSuperati.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Libretto [esami superati: " + esamiSuperati.size());
        sb.append(", media: ").append(String.format("%.2f", getMediaPonderata()));
        sb.append(", CFU totali: ").append(getTotaleCfu());
        sb.append("]");
        return sb.toString();
    }
}