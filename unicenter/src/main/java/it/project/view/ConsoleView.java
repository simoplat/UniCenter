package it.project.view;

import java.time.format.DateTimeFormatter;
import java.util.List;

import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Materia;
import it.project.Studente;

/**
 * Implementazione della View per interfaccia a riga di comando (CLI/Console).
 */
public class ConsoleView implements UniCenterView {

    private final DateTimeFormatter formatterStampa = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
    private final DateTimeFormatter formatterTermine = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void mostraMessaggio(String messaggio) {
        System.out.println(messaggio);
    }

    @Override
    public void mostraErrore(String errore) {
        System.err.println("[ERRORE] " + errore);
    }

    @Override
    public void stampaAppelli(List<Appello> appelli) {
        if (appelli == null || appelli.isEmpty()) {
            mostraMessaggio("Nessun appello disponibile.");
            return;
        }
        for (Appello appello : appelli) {
            String dataOraFormattata = appello.getDataOra().format(formatterStampa);
            String dataTermineIscrizioneFormattata = appello.getTermineIscrizione().format(formatterTermine);

            mostraMessaggio("\n--- Informazioni Appello ---");
            mostraMessaggio("Codice Appello: " + appello.getCodiceAppello());
            mostraMessaggio("Codice Materia: " + appello.getCodiceMateria());
            mostraMessaggio("Data e Ora: " + dataOraFormattata);
            mostraMessaggio("Data Termine Iscrizione: " + dataTermineIscrizioneFormattata);
            mostraMessaggio("Aula: " + appello.getAula());
            mostraMessaggio("Posti Disponibili: " + appello.getPostiDisponibili());
            if (!appello.getVincoloLetteraCognome().isEmpty()) {
                mostraMessaggio("Vincolo Cognome: " + appello.getVincoloLetteraCognome());
            }
            mostraMessaggio("Studenti Prenotati: " + (appello.getIscritti() != null ? appello.getIscritti().size() : 0));
            mostraMessaggio("------------------------------------------");
        }
    }

    @Override
    public void stampaMaterie(List<Materia> materie) {
        if (materie == null || materie.isEmpty()) {
            mostraMessaggio("Nessuna materia disponibile.");
            return;
        }
        for (Materia m : materie) {
            mostraMessaggio(
                    "Codice Materia: " + m.getCodiceMateria() + " | Nome: " + m.getNome() + " | CFU: " + m.getCfu());
        }
    }

    @Override
    public void stampaStudenti(List<Studente> studenti) {
        if (studenti == null || studenti.isEmpty()) {
            mostraMessaggio("Nessuno studente presente.");
            return;
        }
        for (Studente s : studenti) {
            mostraMessaggio("Nome: " + s.getNome() + " | Cognome: " + s.getCognome() + " | Matricola: "
                    + s.getMatricola() + " | Email: " + s.getEmail());
        }
    }

    @Override
    public void stampaCorsiDiLaurea(List<CorsoDiLaurea> corsi) {
        if (corsi == null || corsi.isEmpty()) {
            mostraMessaggio("Nessun corso di laurea presente.");
            return;
        }
        for (CorsoDiLaurea c : corsi) {
            mostraMessaggio(
                    "Nome: " + c.getNome() + " | Tipologia: " + c.getTipologia() + " | Anni: " + c.getAnniAccademici());
        }
    }

    @Override
    public void stampaEsiti(List<EsameSostenuto> esiti) {
        if (esiti == null || esiti.isEmpty()) {
            mostraMessaggio("Nessun esito registrato.");
            return;
        }
        for (EsameSostenuto esito : esiti) {
            mostraMessaggio("ID Verbale: " + esito.getIdVerbale()
                    + " | Materia: " + esito.getCodiceMateria()
                    + " | Voto: " + esito.getVotoNumerico() + (esito.isLode() ? " e Lode" : "")
                    + " | Stato: " + esito.getNomeStato()
                    + (esito.getScadenzaConferma() != null
                            ? " | Scadenza: " + esito.getScadenzaConferma().format(formatterStampa)
                            : "")
                    + (esito.getDataRegistrazione() != null
                            ? " | Registrato il: " + esito.getDataRegistrazione().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : ""));
        }
    }
}
