package it.project.controller;

import it.project.Appello;
import it.project.Materia;
import it.project.Studente;
import it.project.notification.INotificaService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.project.ConsoleUI;
import it.project.Unicenter;
import it.project.validation.IscrizioneValidator;

public class GestioneAppelliController {
    private final INotificaService notificaService;
    private final IscrizioneValidator validatorChain;
    private final List<Appello> appelli;
    Unicenter unicenter = Unicenter.getInstance();
    ConsoleUI console = ConsoleUI.getInstance();

    public GestioneAppelliController(INotificaService notificaService, IscrizioneValidator validatorChain) {
        this.notificaService = notificaService;
        this.validatorChain = validatorChain;
        this.appelli = new ArrayList<>();
    }

    public boolean creaNuovoAppello(Appello appello) {
        // VERIFICA DELLA DATA (UC1 - Controllo Dati Invalidi)
        String codiceMateria = appello.getCodiceMateria();
        LocalDateTime dataOra = appello.getDataOra();
        String aula = appello.getAula();
        int postiDisponibili = appello.getPostiDisponibili();
        String vincoloLetteraCognome = appello.getVincoloLetteraCognome();

        if (dataOra == null) {
            return false; // Data non valida
        }

        if (dataOra.isBefore(LocalDateTime.now())) {
            return false;
        }

        // Creazione dell'appello mediante Factory Method dell'entità Materia
        appelli.add(appello);

        // Pattern Observer: Invio notifiche agli studenti iscritti al corso
        // da fare le notifiche

        return true;
    }

    public boolean iscriviStudente(Studente currentUser, String codiceAppello) {
        Appello appello = trovAppelloById(codiceAppello);
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

}