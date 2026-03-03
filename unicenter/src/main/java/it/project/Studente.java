package it.project;

import java.util.Scanner;

public class Studente extends Utente {
    
    private Carriera carriera;


    @Override
    public void menuPersonale(Scanner sc) {

    
        int scelta = -1;
        while (scelta != 0) {
            try {
                System.out.println("\n--- MENU STUDENTE ---");
                System.out.println("1 - Crea Carriera (Immatricolazione)");
                System.out.println("2 - Visualizza Stato Carriera");
                System.out.println("0 - Logout");
                System.out.print("> ");

                scelta = Integer.parseInt(sc.nextLine());

                switch (scelta) {
                    case 1:
                        creaCarriera(sc);
                        break;
                    case 2:
                        System.out.println(carriera != null ? carriera : "Nessuna carriera attiva.");
                        break;
                    case 0:
                        System.out.println("Uscita...");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Errore: Inserisci un numero valido.");
            }
        }
    }

    public void creaCarriera(Scanner sc){
        this.carriera = new Carriera(0, 0);
    }

    public Studente(Carriera carriera) {
        this.carriera = carriera;
    }

    public Carriera getCarriera() {
        return carriera;
    }

    public void setCarriera(Carriera carriera) {
        this.carriera = carriera;
    }

    

}
