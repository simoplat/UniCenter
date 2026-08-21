package it.project.state;

import it.project.PianoDiStudi;

/**
 * Stato in attesa di approvazione manuale da parte dell'amministratore
 * (quando una o più materie a scelta non sono pre-approvate).
 * Non consente la ri-compilazione diretta finché non viene approvato o rifiutato.
 */
public class StatoInAttesaPiano implements IStatoPianoDiStudi {

    @Override
    public void registra(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano richiede approvazione manuale dell'amministratore.");
    }

    @Override
    public void richiediApprovazione(PianoDiStudi piano) {
        throw new IllegalStateException("La richiesta di approvazione è già stata inoltrata.");
    }

    @Override
    public void approva(PianoDiStudi piano) {
        piano.setStato(new StatoApprovatoPiano());
    }

    @Override
    public void rifiuta(PianoDiStudi piano) {
        piano.setStato(new StatoRifiutatoPiano());
    }

    @Override
    public void ricompila(PianoDiStudi piano) {
        throw new IllegalStateException("Impossibile modificare il piano mentre è in attesa di approvazione. Attendi la risposta dell'amministratore.");
    }

    @Override
    public String getNome() {
        return "In Attesa";
    }

    @Override
    public boolean isApprovato() {
        return false;
    }

    @Override
    public String toString() {
        return getNome();
    }
}
