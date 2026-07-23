package it.project;
import it.project.strategy.ICalcoloTasseStrategy;

public class Studente extends Utente {
    private String matricola;
    private String corsoDiLaurea;
    private boolean tassePagate;
    private double totaleTasse;
    private PianoDiStudi pianoStudi; 

    public Studente(String matricola, String nome, String cognome, String email, String corsoDiLaurea) {
        
        super(nome, cognome, email);
        this.setNome(nome);
        this.setCognome(cognome);
        this.setEmail(email);
        
        // Imposta i campi specifici di Studente
        this.matricola = matricola;
        this.corsoDiLaurea = corsoDiLaurea;
        this.tassePagate = false;
        this.pianoStudi = new PianoDiStudi();
    }

    
    @Override
    public void menuPersonale() {
        System.out.println("=== MENU STUDENTE ===");
        System.out.println("1. Visualizza libretto / carriera");
        System.out.println("2. Iscriviti ad un appello d'esame");
        System.out.println("3. Gestisci voti proposti");
        System.out.println("4. Logout");
        // Qui potrai inserire la logica di navigazione specifica dello studente
    }

    public void calcolaImportoTasse(ICalcoloTasseStrategy strategy, double tassaBaseCorso, boolean isFuoriCorso) {
        this.totaleTasse = strategy.calcolaTasse(tassaBaseCorso, isFuoriCorso);
    }

    public PianoDiStudi getPianoStudi() {
        return pianoStudi;
    }

    public void setPianoStudi(PianoDiStudi pianoStudi) {
        this.pianoStudi = pianoStudi;
    }

    public String getMatricola() { return matricola; }
    public String getCorsoDiLaurea() { return corsoDiLaurea; }
    public boolean isTassePagate() { return tassePagate; }
    public void setTassePagate(boolean tassePagate) { this.tassePagate = tassePagate; }
    public double getTotaleTasse() { return totaleTasse; }
}