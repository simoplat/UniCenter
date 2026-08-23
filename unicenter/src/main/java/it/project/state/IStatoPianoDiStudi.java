package it.project.state;

import it.project.PianoDiStudi;

/**
 * State Pattern (GoF Comportamentale) – Interfaccia per gli stati del Piano di Studi.
 * Ogni stato concreto decide quali transizioni sono lecite.
 * 
 * Transizioni:
 *   StatoBozzaPiano → StatoRegistratoPiano (auto-approvazione, tutte pre-approvate)
 *   StatoBozzaPiano → StatoInAttesaPiano (richiesta approvazione manuale)
 *   StatoInAttesaPiano → StatoApprovatoPiano (approvato dall'amministratore)
 *   StatoInAttesaPiano → StatoRifiutatoPiano (rifiutato dall'amministratore)
 *   StatoRegistratoPiano → StatoBozzaPiano (ri-compilazione)
 *   StatoApprovatoPiano → StatoBozzaPiano (ri-compilazione)
 *   StatoRifiutatoPiano → StatoBozzaPiano (ri-compilazione)
 *
 * Vincolo di compilazione: la ri-compilazione è bloccata dal PianoStudiController
 * se lo studente ha un appello prenotato o un esito pendente per una materia a scelta.
 */
public interface IStatoPianoDiStudi {
    /**
     * Esegue l'auto-approvazione e registra direttamente il piano.
     *
     * @param piano piano di studi su cui applicare la transizione
     */
    void registra(PianoDiStudi piano);

    /**
     * Invia il piano in valutazione per approvazione manuale dell'amministratore.
     *
     * @param piano piano di studi su cui applicare la transizione
     */
    void richiediApprovazione(PianoDiStudi piano);

    /**
     * L'amministratore approva il piano di studi.
     *
     * @param piano piano di studi su cui applicare la transizione
     */
    void approva(PianoDiStudi piano);

    /**
     * L'amministratore rifiuta il piano di studi.
     *
     * @param piano piano di studi su cui applicare la transizione
     */
    void rifiuta(PianoDiStudi piano);

    /**
     * Riporta il piano in bozza per una nuova compilazione o modifica.
     *
     * @param piano piano di studi su cui applicare la transizione
     */
    void ricompila(PianoDiStudi piano);

    /**
     * Restituisce il nome descrittivo dello stato.
     *
     * @return nome dello stato (es. "Bozza", "In Attesa", "Approvato", "Registrato", "Rifiutato")
     */
    String getNome();

    /**
     * Indica se il piano di studi si trova in uno stato approvato/valido.
     *
     * @return true se il piano è approvato o registrato, false altrimenti
     */
    boolean isApprovato();
}
