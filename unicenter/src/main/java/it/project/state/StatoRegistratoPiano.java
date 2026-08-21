package it.project.state;

import it.project.PianoDiStudi;

/**
 * Stato di auto-approvazione del Piano di Studi (quando tutte le materie a scelta sono pre-approvate).
 * Considerato approvato ai fini dell'iscrizione agli appelli.
 * Consente la ri-compilazione tornando in Bozza.
 */
public class StatoRegistratoPiano implements IStatoPianoDiStudi {

    @Override
    public void registra(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano di studi è già registrato (auto-approvato).");
    }

    @Override
    public void richiediApprovazione(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano di studi è già registrato e approvato.");
    }

    @Override
    public void approva(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano di studi è già approvato automaticamente.");
    }

    @Override
    public void rifiuta(PianoDiStudi piano) {
        throw new IllegalStateException("Un piano registrato automaticamente non può essere rifiutato dall'amministratore.");
    }

    @Override
    public void ricompila(PianoDiStudi piano) {
        piano.setStato(new StatoBozzaPiano());
    }

    @Override
    public String getNome() {
        return "Registrato";
    }

    @Override
    public boolean isApprovato() {
        return true;
    }

    @Override
    public String toString() {
        return getNome();
    }
}
