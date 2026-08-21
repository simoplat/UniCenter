package it.project.strategy;

import it.project.PianoDiStudi;

/**
 * Strategia di approvazione manuale:
 * Applicata quando almeno una materia a scelta non è pre-approvata per il corso di laurea.
 * Invoca `piano.richiediApprovazione()` portando il piano in stato `In Attesa`.
 */
public class ApprovazioneManuale implements PoliticaApprovazione {

    @Override
    public void applica(PianoDiStudi piano) {
        piano.richiediApprovazione();
    }
}
