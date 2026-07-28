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
        this.appelli = null;
    }

    public Appello creaNuovoAppello(Materia materia, LocalDateTime dataOra, String aula, int posti, String vincoloCognome, List<Studente> studentiIscrittiCorso) {
        // VERIFICA DELLA DATA (UC1 - Controllo Dati Invalidi)
        if (dataOra == null) {
            throw new IllegalArgumentException("Impossibile creare l'appello: la data e l'ora non possono essere nulle.");
        }

        if (dataOra.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data o ora non valida: non è possibile inserire un appello nel passato (" + dataOra + ").");
        }

        if (materia == null) {
            throw new IllegalArgumentException("Impossibile creare l'appello: la materia non è valida.");
        }

        // Creazione dell'appello mediante Factory Method dell'entità Materia
        Appello appello = materia.creaAppello(dataOra, aula, posti, vincoloCognome);

        // Pattern Observer: Invio notifiche agli studenti iscritti al corso
        if (notificaService != null && studentiIscrittiCorso != null) {
            String messaggio = "Nuovo appello inserito per la materia " + materia.getNome() + " previsto per il: " + dataOra;
            for (Studente s : studentiIscrittiCorso) {
                notificaService.inviaNotifica(s.getEmail(), messaggio);
            }
        }

        return appello;
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
                "Iscrizione confermata per l'appello " + appello.getCodiceAppello()
            );

            return true;
        } catch (Exception e) {
            console.mostraErrore("[ERRORE ISCRIZIONE] " + e.getMessage());
            return false;
        }
    }

    public List <Appello> trovaAppelliDisponibili(List <String> codiciMaterie) {
        List <Appello> appelliDisponibili = new ArrayList<>();;
        for (String codiceMateria : codiciMaterie) {
            for (Appello ap: appelli) {
                if (ap.getCodiceMateria().equals(codiceMateria)) {
                    appelliDisponibili.add(ap);
                }
            }
        }
        return appelliDisponibili;        
    }


    public Appello trovAppelloById(String codiceAppello){
        for (Appello app: appelli){
            if (app.getCodiceAppello().equals(codiceAppello)){
                return app;
            }
        } return null;
    }

}