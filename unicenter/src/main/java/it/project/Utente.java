package it.project;
import java.util.Scanner;

public class Utente {
    
    private int id;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String codiceFiscale;

    

    public Utente(int id, String nome, String cognome, String email, String password, String codiceFiscale) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.codiceFiscale = codiceFiscale;
    }



    public void menuPersonale(Scanner sc) {
        System.out.println("Menu personale");
    }
    
}
