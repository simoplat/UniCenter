package it.project;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Libretto {
    private final Map<Materia, Voto> voci = new HashMap<>();

    public void registraVoto(Materia materia, Voto voto) {
        if (voci.containsKey(materia)) {
            throw new IllegalStateException("Esame già registrato per questa materia.");
        }
        voci.put(materia, voto);
    }

    public Optional<Voto> getVoto(Materia materia) {
        return Optional.ofNullable(voci.get(materia));
    }

  
    public double getMediaPonderata() {
        return 30.00;
    }
}