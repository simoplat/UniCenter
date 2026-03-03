package it.project;

public class Materia {
    private int idMateria;
    private int crediti;
    private String nome;
    
    public Materia(int idMateria, int crediti, String nome) {
        this.idMateria = idMateria;
        this.crediti = crediti;
        this.nome = nome;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }

    public int getCrediti() {
        return crediti;
    }

    public void setCrediti(int crediti) {
        this.crediti = crediti;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    
}
