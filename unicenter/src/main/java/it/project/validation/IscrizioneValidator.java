package it.project.validation;

import it.project.Appello;
import it.project.Studente;

public interface IscrizioneValidator {
    void setNext(IscrizioneValidator nextValidator);
    boolean validate(Studente studente, Appello appello) throws Exception;
}