package it.project.state;

import it.project.EsameSostenuto;

/**
 * Stato finale: il voto è stato rifiutato dallo studente.
 * Lo studente potrà iscriversi a un appello futuro per la stessa materia.
 */
public class RifiutatoState implements IStatoVoto {

    @Override
    public void accetta(EsameSostenuto esame) {
        throw new IllegalStateException("Il voto è già stato rifiutato. Non è più possibile accettarlo.");
    }

    @Override
    public void rifiuta(EsameSostenuto esame) {
        throw new IllegalStateException("Il voto è già stato rifiutato.");
    }

    @Override
    public String getNome() {
        return "Rifiutato";
    }
}
