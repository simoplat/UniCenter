package it.project.strategy;

import it.project.PianoDiStudi;

/**
 * Strategia di approvazione automatica:
 * Applicata quando tutte le materie a scelta selezionate dallo studente
 * sono pre-approvate per il suo Corso di Laurea.
 * Invoca `piano.registra()` portando il piano in stato `Registrato`.
 */
public class ApprovazioneAutomatica implements PoliticaApprovazione {

    @Override
    public void applica(PianoDiStudi piano) {
        piano.registra();
    }
}
