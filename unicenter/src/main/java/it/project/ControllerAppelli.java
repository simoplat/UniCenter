package it.project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.project.exceptions.UtenteNonTrovatoException;

public class ControllerAppelli {
    // 1. Corretto il tipo della chiave da Integer a String
    private Map<String, Appello> appelli;

    // Costruttore con inizializzazione di sicurezza se passa null
    public ControllerAppelli(Map<String, Appello> appelli) {
        this.appelli = (appelli != null) ? appelli : new HashMap<>();
    }

    // Costruttore di default vuoto (utile per l'inizializzazione rapida)
    public ControllerAppelli() {
        this.appelli = new HashMap<>();
    }

    public Map<String, Appello> getAppelli() {
        return Collections.unmodifiableMap(appelli);
    }

    public void setAppelli(Map<String, Appello> appelli) {
        this.appelli = (appelli != null) ? appelli : new HashMap<>();
    }

    public void aggiungiAppello(Appello p) {
        if (p == null || p.getCodiceAppello() == null) {
            throw new IllegalArgumentException("L'appello o il suo codice non possono essere null.");
        }
        // Ora il tipo della chiave (String) coincide con p.getCodiceAppello()
        this.appelli.put(p.getCodiceAppello(), p);
    }

    /**
     * Cerca un professore all'interno della mappa dei professori.
     */
    public Professore trovaProfessore(String idProfessore, Map<String, Professore> profMap) throws UtenteNonTrovatoException {
        if (profMap == null || !profMap.containsKey(idProfessore)) {
            throw new UtenteNonTrovatoException("Professore non trovato con ID: " + idProfessore);
        }
        return profMap.get(idProfessore);
    }
}