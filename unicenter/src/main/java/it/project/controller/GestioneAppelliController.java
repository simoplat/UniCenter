package it.project.controller;

import it.project.Appello;
import it.project.Studente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.project.ConsoleUI;
import it.project.Notifica;
import it.project.Professore;
import it.project.Unicenter;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.PostiNonValidi;
import it.project.generator.CodiceAppelloGenerator;
import it.project.validation.*;

public class GestioneAppelliController {;
    private IscrizioneValidator validatorChain;
    private final List<Appello> appelli;
    private final CodiceAppelloGenerator codiceAppelloGenerator;
    Unicenter unicenter;
    ConsoleUI console = ConsoleUI.getInstance();

    public GestioneAppelliController(Unicenter unicenter) {
        this.unicenter = unicenter;
        this.validatorChain = ValidationChainBuilder.buildDefaultChain();
        this.appelli = new ArrayList<>();
        this.codiceAppelloGenerator = CodiceAppelloGenerator.getInstance();
    }

    public boolean creaNuovoAppello(String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili, String vincoloLetteraCognome, LocalDate termineIscrizione) throws Exception {
        
        
        
        
        if(!validateAppello(codiceMateria, dataOraStr, aula, postiDisponibili, vincoloLetteraCognome, termineIscrizione)) {
            return false;
        }
        
        String codiceAppello = codiceAppelloGenerator.generateCodice();
        Appello appello = new Appello(codiceAppello, codiceMateria, dataOraStr, aula, postiDisponibili, vincoloLetteraCognome, termineIscrizione);
        appelli.add(appello);
        
        // Pattern Observer: Invio notifiche agli studenti iscritti al corso, da fare le notifiche

        return true;
    }

    public boolean validateAppello(String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili, String vincoloLetteraCognome, LocalDate termineIscrizione) {
        if (dataOraStr == null || dataOraStr.isBefore(LocalDateTime.now()) || termineIscrizione.isAfter(dataOraStr.toLocalDate())) {
            return false;
        }
        
        if (postiDisponibili <= 0) {
            return false;
        }


        if (unicenter.getCurrentUser() != null && unicenter.getCurrentUser() instanceof Professore) {
            if (!unicenter.isProfessoreAbilitatoAMateria(codiceMateria)) {
                return false;
            }
        }
        return true;

    }

    public boolean iscriviStudente(Studente studente, String codiceAppello) {
        Appello appello = trovAppelloByIdAppello(codiceAppello);
        
        if (this.validatorChain == null) {
            this.validatorChain = ValidationChainBuilder.buildDefaultChain();
        }
        
        if (appello == null || appello.getIscritti().contains(studente)) {
            return false; // Appello non trovato o studente già iscritto
        }
        try {
            // Esegue i controlli della Chain of Responsibility
            validatorChain.validate(studente, appello);

            // Se la validazione passa, registra l'iscritto
            appello.aggiungiIscritto(studente);
            String messaggio = "Ti sei iscritto all'appello: " + appello.toString();
            Notifica nuovaNotifica = new Notifica("Iscrizione Appello", messaggio, LocalDateTime.now());

            // Invia la notifica allo studente
            studente.riceviNotifica(nuovaNotifica);
                

            return true;
        } catch (Exception e) {
            console.mostraErrore(e.getMessage());
            return false;
        }

    }

    public boolean disiscriviStudente(Studente studente, String codiceAppello) {
        Appello appello = trovAppelloByIdAppello(codiceAppello);
        if (appello == null) {
            return false; // Appello non trovato
        }
        if (appello.getIscritti().contains(studente)) {
            appello.rimuoviIscritto(studente);
            String messaggio = "Ti sei disiscritto dall'appello: " + appello.toString();
            Notifica nuovaNotifica = new Notifica("[Disiscrizione Appello]", messaggio, LocalDateTime.now());
            studente.riceviNotifica(nuovaNotifica);
            return true;
        } else {
            return false; 
        }
    }

    public List<Appello> trovaAppelliByIdMateria(List<String> codiciMaterie) {
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

    public List <Appello> trovaAppelliPrenotabiliByStudente(Studente studente, List<String> codiciMaterie) {
        List<Appello> appelliById = trovaAppelliByIdMateria(codiciMaterie);
        List <Appello> appelliPrenotabili = new ArrayList<>();
        for (Appello app: appelliById) {
            if (!app.getIscritti().contains(studente)) {
                appelliPrenotabili.add(app);
            }
        }
        return appelliPrenotabili;
    }

    public Appello trovAppelloByIdAppello(String codiceAppello) {
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

    public List<Studente> trovaIscrittiByIdAppello(String codiceAppello){
        Appello appello = trovAppelloByIdAppello(codiceAppello);
        return appello.getIscritti();
    }

    public boolean modificaAppello(String codiceAppello, LocalDateTime dataOra, String aula, int postiDisponibili, String vincolo, LocalDate dataTermineIscrizione){
        
        
        for (Appello a : appelli){
            if (a.getCodiceAppello().equals(codiceAppello)) {
                        String codiceMateria = a.getCodiceMateria();
                        if(!validateAppello(codiceMateria, dataOra, aula, postiDisponibili, vincolo, dataTermineIscrizione)) {
                            return false;
                        }
                        a.setDataOra(dataOra);
                        a.setAula(aula);
                        a.setPostiDisponibili(postiDisponibili);
                        a.setVincoloLetteraCognome(vincolo);
                        a.setTermineIscrizione(dataTermineIscrizione);
                        String oggetto = "Modifica appello : " + codiceAppello;
                        String contenuto = "L'appello " + codiceAppello + " è stato modificato.\n" + 
                                            "Orario: " + dataOra + "\n" +
                                            "Aula: " + aula+ "\n" + 
                                            "Posti disponibili " + postiDisponibili + "\n" +
                                            "Vincolo cognome " + vincolo + "\n" +
                                            "Data termine iscrizione: " + dataTermineIscrizione + "\n";
                        LocalDateTime ora = LocalDateTime.now();
                        Notifica notifica = new Notifica(oggetto, contenuto , ora);
                        a.notifica(notifica);
                        return true;
                        }
        }
        return false;
    }

    public boolean eliminaAppello(String codiceAppello) {
    for (Appello a : appelli) {
        if (a.getCodiceAppello().equals(codiceAppello)) {

            String oggetto = " Eliminazione appello " + codiceAppello ;
            String contenuto = "L'appello " + codiceAppello + " è stato eliminato. "; 
            Notifica notifica = new Notifica(oggetto, contenuto , LocalDateTime.now());
            a.notifica(notifica);
            appelli.remove(a); 
            return true;       
        }
    }
    return false;
    }

    public List<Appello> appelliPrenotatiByStudente(Studente studente) {
        List<Appello> appelliPrenotati = new ArrayList<>();
        for (Appello appello : appelli) {
            if (appello.getIscritti().contains(studente)) {
                appelliPrenotati.add(appello);
            }
        }
        return appelliPrenotati;
    }
}