package it.project;
import java.util.Collections;
import java.util.Map;


import it.project.exceptions.UtenteNonTrovatoException;

public class ControllerAppelli {
    private Map<Integer, Appello> appelli;

    public ControllerAppelli(Map<Integer, Appello> appelli) {
        this.appelli = appelli;
    }

    public Map<Integer, Appello> getAppelli() {
        return Collections.unmodifiableMap(appelli);
    }

    public void setAppelli(Map<Integer, Appello> appelli) {
        this.appelli = appelli;
    }

    public void aggiungiAppello(Appello p) {
        this.appelli.put(p.getIdAppello(), p);
    }

    public Professore trovaProfessore (int idProfessore, Map<Integer, Professore> profMap) throws UtenteNonTrovatoException {

            if (!profMap.containsKey(idProfessore)) {
                throw new UtenteNonTrovatoException("Professore non Trovato");
            }
    Professore p = profMap.get(idProfessore);
    return p;
}
}
