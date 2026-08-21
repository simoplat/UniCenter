package it.project.state;

import it.project.PianoDiStudi;

/**
 * Stato di approvazione da parte dell'amministratore.
 * Consente l'iscrizione a tutte le materie (obbligatorie e a scelta).
 * Consente la ri-compilazione tornando in Bozza (se i vincoli su appelli/esiti lo permettono).
 */
public class StatoApprovatoPiano implements IStatoPianoDiStudi {

    @Override
    public void registra(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano di studi è già approvato.");
    }

    @Override
    public void richiediApprovazione(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano di studi è già approvato.");
    }

    @Override
    public void approva(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano di studi è già stato approvato.");
    }

    @Override
    public void rifiuta(PianoDiStudi piano) {
        throw new IllegalStateException("Un piano già approvato non può essere rifiutato direttamente.");
    }

    @Override
    public void ricompila(PianoDiStudi piano) {
        piano.setStato(new StatoBozzaPiano());
    }

    @Override
    public String getNome() {
        return "Approvato";
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
