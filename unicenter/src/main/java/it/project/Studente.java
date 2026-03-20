package it.project;

public class Studente extends Utente {

    private Carriera carriera;

    public void creaCarriera() {
        if (this.carriera != null) {
            System.out.println("Sei già iscritto a un corso");
            return;
        } else
            this.carriera = new Carriera(0, 0);
    }

    public Studente(Carriera carriera, int id, String nome, String cognome, String email, String password,
            String codiceFiscale) {
        super(id, nome, cognome, email, password, codiceFiscale);
        this.carriera = carriera;
    }

    @Override
    public void menuPersonale() {
        consoleUi.mostraMessaggio("Benvenuto Studente:" + toString());
        int run = 1;
        while (run == 1) {
            consoleUi.mostraMessaggio("1) Visualizza Appelli");
            consoleUi.mostraMessaggio("2) Aggiungi un Appello");
            consoleUi.mostraMessaggio("3) Modifica un Appello");
            consoleUi.mostraMessaggio("4) Rimuovi un Appello");
            consoleUi.mostraMessaggio("5) Esci");

            Integer scelta = consoleUi.leggiIntero("Seleziona un'opzione: ");
            try {
                switch (scelta) {
                    case 1:
                        
                        break;
                    case 2:
                        
                        break;
                    case 3:
                        
                        break;
                    case 4:
                        
                        break;
                    case 5:
                        consoleUi.mostraMessaggio("Uscita dal programma...");
                        run = 0;
                        break;
                    default:
                        consoleUi.mostraMessaggio("Opzione non valida, riprova.");
                        break;
                }
            } catch (NumberFormatException e) {
                consoleUi.mostraErrore("Inserisci un valore intero, riprova.");

            } catch (Exception e) {
                consoleUi.mostraErrore("Si è verificato un errore, riprova.");
            }
        }
    }

    public Carriera getCarriera() {
        return carriera;
    }

    public void setCarriera(Carriera carriera) {
        this.carriera = carriera;
    }


    public void visualizzaCarriera() {
        if (this.carriera == null) {
            System.out.println("Carriera attiva: " + this.carriera);
        } else
            System.out.println("Non hai una carriera attiva al momento");
    }

    
}
