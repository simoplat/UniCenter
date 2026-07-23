package it.project.controller;

import it.project.Appello;
import it.project.Studente;
import it.project.notification.INotificaService;
import it.project.validation.IscrizioneValidator;

public class IscrizioneAppelloController {
    private final INotificaService notificaService;
    private final IscrizioneValidator validatorChain;

    // AGGIUNGI / AGGIORNA QUESTO COSTRUTTORE A 2 PARAMETRI
    public IscrizioneAppelloController(INotificaService notificaService, IscrizioneValidator validatorChain) {
        this.notificaService = notificaService;
        this.validatorChain = validatorChain;
    }

    public boolean iscriviStudente(Studente studente, Appello appello) {
        try {
            // Esegue i controlli della Chain of Responsibility
            validatorChain.validate(studente, appello);
            
            // Se la validazione passa, registra l'iscritto
            appello.aggiungiIscritto(studente);

            // Invia la notifica via adapter
            notificaService.inviaNotifica(
                studente.getEmail(),
                "Iscrizione confermata per l'appello " + appello.getCodiceAppello()
            );

            return true;
        } catch (Exception e) {
            System.err.println("[ERRORE ISCRIZIONE] " + e.getMessage());
            return false;
        }
    }
}