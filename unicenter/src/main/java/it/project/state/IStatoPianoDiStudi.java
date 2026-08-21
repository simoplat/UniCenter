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
    void registra(PianoDiStudi piano);              // auto-approva
    void richiediApprovazione(PianoDiStudi piano);  // invia in attesa
    void approva(PianoDiStudi piano);               // admin approva
    void rifiuta(PianoDiStudi piano);               // admin rifiuta
    void ricompila(PianoDiStudi piano);             // torna in bozza per ri-compilazione
    String getNome();
    boolean isApprovato();
}
