package it.project.state;

import it.project.EsameSostenuto;

/**
 * Stato finale: il voto è stato accettato dallo studente e verbalizzato.
 * Nessuna transizione ulteriore è consentita.
 */
public class ApprovatoState implements IStatoVoto {

    @Override
    public void accetta(EsameSostenuto esame) {
        throw new IllegalStateException("Il voto è già stato approvato e non è più modificabile.");
    }

    @Override
    public void rifiuta(EsameSostenuto esame) {
        throw new IllegalStateException("Il voto è già stato approvato e non è più modificabile.");
    }

    @Override
    public String getNome() {
        return "Approvato";
    }
}
