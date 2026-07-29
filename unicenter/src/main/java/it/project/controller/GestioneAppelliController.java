package it.project.controller;

import it.project.Appello;
import it.project.Studente;
import it.project.notification.INotificaService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.project.ConsoleUI;
import it.project.Unicenter;
import it.project.generator.CodiceAppelloGenerator;
import it.project.validation.IscrizioneValidator;
import it.project.validation.ValidationChainBuilder;

public class GestioneAppelliController {
    private final INotificaService notificaService;
    private IscrizioneValidator validatorChain;
    private final List<Appello> appelli;
    private final CodiceAppelloGenerator codiceAppelloGenerator;
    Unicenter unicenter = Unicenter.getInstance();
    ConsoleUI console = ConsoleUI.getInstance();

    public GestioneAppelliController(INotificaService notificaService) {
        this.notificaService = notificaService;
        this.validatorChain = ValidationChainBuilder.buildDefaultChain();
        this.appelli = new ArrayList<>();
        this.codiceAppelloGenerator = CodiceAppelloGenerator.getInstance();
    }

    public boolean creaNuovoAppello(Appello appello) {
        
        LocalDateTime dataOra = appello.getDataOra();
        int postiDisponibili = appello.getPostiDisponibili();
        if (dataOra == null) {
            return false; 
        }

        if (dataOra.isBefore(LocalDateTime.now())) {
            return false;
        }

        if (postiDisponibili <= 0) {
            return false;
        }

        appelli.add(appello);

        // Pattern Observer: Invio notifiche agli studenti iscritti al corso, da fare le notifiche

        return true;
    }

    public boolean iscriviStudente(Studente currentUser, String codiceAppello) {
        Appello appello = trovAppelloById(codiceAppello);
        
        if (this.validatorChain == null) {
            this.validatorChain = ValidationChainBuilder.buildDefaultChain();
        }
        
        if (appello == null) {
            return false; // Appello non trovato
        }
        try {
            // Esegue i controlli della Chain of Responsibility
            validatorChain.validate(currentUser, appello);

            // Se la validazione passa, registra l'iscritto
            appello.aggiungiIscritto(currentUser);

            // Invia la notifica via adapter
            notificaService.inviaNotifica(
                    currentUser.getEmail(),
                    "Iscrizione confermata per l'appello " + appello.getCodiceAppello());

            return true;
        } catch (Exception e) {
            console.mostraErrore("[ERRORE ISCRIZIONE] " + e.getMessage());
            return false;
        }

    }

    public List<Appello> trovaAppelliDisponibili(List<String> codiciMaterie) {
        if (appelli == null || appelli.isEmpty()) {
            return null;
        }
        List<Appello> appelliDisponibili = new ArrayList<>();
        for (String codiceMateria : codiciMaterie) {
            for (Appello ap : appelli) {
                if (ap.getCodiceMateria().equals(codiceMateria)) {
                    appelliDisponibili.add(ap);
                }
            }
        }
        return appelliDisponibili;
    }

    public Appello trovAppelloById(String codiceAppello) {
        for (Appello app : appelli) {
            if (app.getCodiceAppello().equals(codiceAppello)) {
                return app;
            }
        }
        return null;
    }

    public String generaCodiceAppello() {
        return codiceAppelloGenerator.generateCodice();
    }
}