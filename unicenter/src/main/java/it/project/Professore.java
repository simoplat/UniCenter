package it.project;


public class Professore extends Utente {

    public Professore(int id, String nome, String cognome, String email, String password, String codiceFiscale) {
        super(id, nome, cognome, email, password, codiceFiscale);
    }

    // Metodo principale per il professore
    @Override
    public void menuPersonale() {
    
    }

    // Sottomenu specifico per la bacheca
    public void menuAppelli() {
       
    }

    // Metodi di supporto
    private void visualizzaAppelli() {
        System.out.println("Visualizzazione degli appelli in corso...");
    }

    private void aggiungiAppello() {
        System.out.println("Procedura di aggiunta appello avviata.");
    }

    private void modificaAppello() {
        System.out.println("Modifica dell'appello selezionato.");
    }

    private void rimuoviAppello() {
        System.out.println("Appello rimosso con successo.");
    }
}