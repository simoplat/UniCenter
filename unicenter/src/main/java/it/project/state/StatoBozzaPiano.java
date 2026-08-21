package it.project.state;

import it.project.PianoDiStudi;

/**
 * Stato iniziale o di ri-compilazione del Piano di Studi.
 * Consente la transizione a Registrato (auto-approvazione se tutte le materie
 * sono pre-approvate) o a InAttesa (se ci sono materie non pre-approvate).
 */
public class StatoBozzaPiano implements IStatoPianoDiStudi {

    @Override
    public void registra(PianoDiStudi piano) {
        piano.setStato(new StatoRegistratoPiano());
    }

    @Override
    public void richiediApprovazione(PianoDiStudi piano) {
        piano.setStato(new StatoInAttesaPiano());
    }

    @Override
    public void approva(PianoDiStudi piano) {
        throw new IllegalStateException("Un piano in bozza non può essere approvato direttamente: invia prima la richiesta.");
    }

    @Override
    public void rifiuta(PianoDiStudi piano) {
        throw new IllegalStateException("Un piano in bozza non può essere rifiutato direttamente.");
    }

    @Override
    public void ricompila(PianoDiStudi piano) {
        // Già in bozza, nessuna operazione necessaria
    }

    @Override
    public String getNome() {
        return "Bozza";
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
