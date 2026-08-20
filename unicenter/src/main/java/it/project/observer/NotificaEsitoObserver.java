package it.project.observer;

import java.time.LocalDateTime;

import it.project.EsameSostenuto;
import it.project.Notifica;
import it.project.Professore;
import it.project.Studente;

/**
 * Observer concreto: al cambio di stato del voto, notifica lo Studente e il Professore
 * aggiornando la dashboard (lista notifiche) e inviando un riepilogo.
 */
public class NotificaEsitoObserver implements ObserverEsitoVoto {

    private final Studente studente;
    private final Professore professore;

    public NotificaEsitoObserver(Studente studente, Professore professore) {
        this.studente = studente;
        this.professore = professore;
    }

    @Override
    public void aggiornamento(EsameSostenuto esame, String nuovoStato) {
        String oggettoStudente = "Aggiornamento esito esame - " + esame.getCodiceMateria();
        String messaggioStudente = costruisciMessaggioStudente(esame, nuovoStato);
        Notifica notificaStudente = new Notifica(oggettoStudente, messaggioStudente, it.project.database.ClockProvider.nowLocalDateTime());
        studente.aggiungiNotifica(notificaStudente);

        String oggettoProfessore = "Aggiornamento esito esame - " + esame.getCodiceMateria();
        String messaggioProfessore = costruisciMessaggioProfessore(esame, nuovoStato);
        Notifica notificaProfessore = new Notifica(oggettoProfessore, messaggioProfessore, it.project.database.ClockProvider.nowLocalDateTime());
        professore.aggiungiNotifica(notificaProfessore);
    }

    private String costruisciMessaggioStudente(EsameSostenuto esame, String nuovoStato) {
        StringBuilder sb = new StringBuilder();
        sb.append("Il tuo esito per l'esame di ").append(esame.getCodiceMateria());
        sb.append(" (voto: ").append(esame.getVotoNumerico());
        if (esame.isLode()) {
            sb.append(" e Lode");
        }
        sb.append(") è passato allo stato: ").append(nuovoStato).append(".");

        switch (nuovoStato) {
            case "Approvato" -> sb.append("\nIl voto è stato registrato nel tuo libretto.");
            case "Rifiutato" -> sb.append("\nPotrai iscriverti a un appello futuro per questa materia.");
            case "Bocciato" -> sb.append("\nIl voto è insufficiente. Potrai iscriverti a un appello futuro.");
        }
        return sb.toString();
    }

    private String costruisciMessaggioProfessore(EsameSostenuto esame, String nuovoStato) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lo studente ").append(esame.getMatricolaStudente());

        switch (nuovoStato) {
            case "Approvato" -> sb.append(" ha accettato");
            case "Rifiutato" -> sb.append(" ha rifiutato");
            case "Bocciato" -> sb.append(" è stato bocciato per insufficienza su");
            case "In attesa di conferma" -> sb.append(" ha ricevuto l'esito per");
            default -> sb.append(" ha aggiornato lo stato di");
        }

        sb.append(" il voto ").append(esame.getVotoNumerico());
        if (esame.isLode()) {
            sb.append(" e Lode");
        }
        sb.append(" per l'esame di ").append(esame.getCodiceMateria()).append(".");
        sb.append("\nStato attuale: ").append(nuovoStato);
        return sb.toString();
    }
}
