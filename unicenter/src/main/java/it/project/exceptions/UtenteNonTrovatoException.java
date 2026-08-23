package it.project.exceptions;

/**
 * Eccezione unchecked lanciata quando un utente non viene trovato durante l'autenticazione o la ricerca.
 */
public class UtenteNonTrovatoException extends RuntimeException {

    /**
     * Costruttore con messaggio di errore.
     *
     * @param message messaggio descrittivo
     */
    public UtenteNonTrovatoException(String message){
        super(message);
    }

}
