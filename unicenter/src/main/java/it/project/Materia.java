package it.project;

import it.project.generator.CodiceAppelloGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Materia {
    private String codiceMateria;
    private String nome;
    private int cfu;
    private List<Appello> appelli;

    public Materia(String codiceMateria, String nome, int cfu) {
        this.codiceMateria = codiceMateria;
        this.nome = nome;
        this.cfu = cfu;
        this.appelli = new ArrayList<>();
    }

    /**
     * Pattern Creator & Factory Method:
     * La materia crea e registra un nuovo appello associandogli un codice univoco.
     */
    public Appello creaAppello(LocalDateTime dataOra, String aula, int posti, String vincoloCognome) {
        String codiceAppello = CodiceAppelloGenerator.getInstance().generateCodice();
        Appello nuovoAppello = new Appello(codiceAppello, this.codiceMateria, dataOra, aula, posti, vincoloCognome);
        this.appelli.add(nuovoAppello);
        return nuovoAppello;
    }

    public List<Appello> getAppelli() { return appelli; }
    public String getCodiceMateria() { return codiceMateria; }
    public String getNome() { return nome; }
}