package it.project.controller;

import it.project.Appello;
import it.project.Materia;
import it.project.Studente;
import it.project.notification.INotificaService;

import java.time.LocalDateTime;
import java.util.List;

public class GestioneAppelliController {
    private final INotificaService notificaService;

    public GestioneAppelliController(INotificaService notificaService) {
        this.notificaService = notificaService;
    }

    public Appello creaNuovoAppello(Materia materia, LocalDateTime dataOra, String aula, int posti, String vincoloCognome, List<Studente> studentiIscrittiCorso) {
        // =========================================================================
        // VERIFICA DELLA DATA (UC1 - Controllo Dati Invalidi)
        // =========================================================================
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
}