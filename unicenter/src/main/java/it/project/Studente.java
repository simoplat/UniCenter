package it.project;

import java.util.Scanner;

public class Studente extends Utente {
    
    private Carriera carriera;


    @Override
    public void menuPersonale() {

    }

    public void creaCarriera(){
        if(this.carriera != null) {
            System.out.println("Sei già iscritto a un corso");
            return;
        } else
        this.carriera = new Carriera(0, 0);
    }

    public Studente(Carriera carriera, int id, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(id, nome, cognome, email, password, codiceFiscale);
        this.carriera = carriera;
    }

    public Carriera getCarriera() {
        return carriera;
    }

    public void setCarriera(Carriera carriera) {
        this.carriera = carriera;
    }

    public void visualizzaCarriera(){
        if(this.carriera == null) {
            System.out.println("Carriera attiva: "+ this.carriera);
        } else
            System.out.println("Non hai una carriera attiva al momento");
    }
    

}
