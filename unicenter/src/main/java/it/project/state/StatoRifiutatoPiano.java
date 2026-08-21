package it.project.state;

import it.project.PianoDiStudi;

/**
 * Stato di rifiuto da parte dell'amministratore.
 * Consente la ri-compilazione tornando in Bozza per selezionare un nuovo set di materie a scelta.
 */
public class StatoRifiutatoPiano implements IStatoPianoDiStudi {

    @Override
    public void registra(PianoDiStudi piano) {
        throw new IllegalStateException("Un piano rifiutato deve essere ricompilato prima della registrazione.");
    }

    @Override
    public void richiediApprovazione(PianoDiStudi piano) {
        throw new IllegalStateException("Un piano rifiutato deve essere ricompilato prima di richiedere nuova approvazione.");
    }

    @Override
    public void approva(PianoDiStudi piano) {
        throw new IllegalStateException("Un piano rifiutato non può essere approvato direttamente.");
    }

    @Override
    public void rifiuta(PianoDiStudi piano) {
        throw new IllegalStateException("Il piano di studi è già stato rifiutato.");
    }

    @Override
    public void ricompila(PianoDiStudi piano) {
        piano.setStato(new StatoBozzaPiano());
    }

    @Override
    public String getNome() {
        return "Rifiutato";
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
