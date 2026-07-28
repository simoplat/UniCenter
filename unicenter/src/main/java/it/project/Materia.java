package it.project;

public class Materia {
    private String codiceMateria;
    private String nome;
    private int cfu;

    public Materia(String codiceMateria, String nome, int cfu) {
        this.codiceMateria = codiceMateria;
        this.nome = nome;
        this.cfu = cfu;
    }

    public String getCodiceMateria() { return codiceMateria; }
    public String getNome() { return nome; }

    @Override
    public String toString() {
        return "Materia [codiceMateria=" + codiceMateria + ", nome=" + nome + ", cfu=" + cfu + "]";
    }

    
}