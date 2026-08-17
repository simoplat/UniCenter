package it.project.state;

import it.project.EsameSostenuto;

/**
 * Stato finale per voti insufficienti (Regola di Dominio 4).
 * Se il voto è inferiore a 18/30, l'esame viene automaticamente registrato come "Bocciato".
 * Analogo al caso rifiutato: il voto non viene verbalizzato.
 */
public class BocciatoState implements IStatoVoto {

    @Override
    public void accetta(EsameSostenuto esame) {
        throw new IllegalStateException("Esame bocciato (voto insufficiente): non è possibile accettare il voto.");
    }

    @Override
    public void rifiuta(EsameSostenuto esame) {
        throw new IllegalStateException("Esame bocciato (voto insufficiente): il rifiuto è già implicito.");
    }

    @Override
    public String getNome() {
        return "Bocciato";
    }
}
