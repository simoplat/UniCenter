package it.project.validation;

import it.project.Appello;
import it.project.Studente;
import it.project.exceptions.validator.IscrizioneNonValidaException;

/**
 * Handler interface per il Pattern Chain of Responsibility (GoF Comportamentale)
 * per la validazione delle iscrizioni agli appelli.
 */
public interface IscrizioneValidator {
    /**
     * Imposta il prossimo validatore nella catena di responsabilità.
     *
     * @param nextValidator prossimo anello della catena
     */
    void setNext(IscrizioneValidator nextValidator);

    /**
     * Esegue la validazione dell'iscrizione dello studente all'appello.
     *
     * @param studente studente richiedente
     * @param appello  appello a cui iscriversi
     * @return true se valido
     * @throws IscrizioneNonValidaException in caso di violazione di uno dei vincoli di iscrizione
     */
    boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException;
}