package it.project.state;

import it.project.EsameSostenuto;

/**
 * Stato iniziale del voto: consente le transizioni di accettazione o rifiuto.
 */
public class InAttesaConfermaState implements IStatoVoto {

    @Override
    public void accetta(EsameSostenuto esame) {
        esame.setStato(new ApprovatoState());
    }

    @Override
    public void rifiuta(EsameSostenuto esame) {
        esame.setStato(new RifiutatoState());
    }

    @Override
    public String getNome() {
        return "In attesa di conferma";
    }
}
