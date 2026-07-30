package it.project.controller;

import it.project.Appello;
import it.project.Studente;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.project.ConsoleUI;
import it.project.Notifica;
import it.project.Unicenter;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.PostiNonValidi;
import it.project.generator.CodiceAppelloGenerator;
import it.project.validation.IscrizioneValidator;
import it.project.validation.ValidationChainBuilder;

public class GestioneAppelliController {;
    private IscrizioneValidator validatorChain;
    private final List<Appello> appelli;
    private final CodiceAppelloGenerator codiceAppelloGenerator;
    Unicenter unicenter = Unicenter.getInstance();
    ConsoleUI console = ConsoleUI.getInstance();

    public GestioneAppelliController() {
        this.validatorChain = ValidationChainBuilder.buildDefaultChain();
        this.appelli = new ArrayList<>();
        this.codiceAppelloGenerator = CodiceAppelloGenerator.getInstance();
    }

    public boolean creaNuovoAppello(Appello appello) throws Exception {
        
        LocalDateTime dataOra = appello.getDataOra();
        int postiDisponibili = appello.getPostiDisponibili();
        if (dataOra == null) {
            throw new DataNonValidaException("La data e ora non valide.");
            
        }
        
        if (dataOra.isBefore(LocalDateTime.now())) {
            throw new DataNonValidaException("La data e ora non valide.");
        }

        if (postiDisponibili <= 0) {
            throw new PostiNonValidi("Il numero di posti disponibili deve essere maggiore di zero.");
        }

        appelli.add(appello);

        // Pattern Observer: Invio notifiche agli studenti iscritti al corso, da fare le notifiche

        return true;
    }

    public boolean iscriviStudente(Studente currentUser, String codiceAppello) {
        Appello appello = trovAppelloByIdAppello(codiceAppello);
        
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
            String messaggio = "Ti sei iscritto all'appello: " + appello.toString();
            Notifica nuovaNotifica = new Notifica("Iscrizione Appello", messaggio, LocalDateTime.now());

            // Invia la notifica allo studente
            currentUser.riceviNotifica(nuovaNotifica);
                

            return true;
        } catch (Exception e) {
            console.mostraErrore("[ERRORE ISCRIZIONE] " + e.getMessage());
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

    public boolean modificaAppello(String codiceAppello, LocalDateTime dataOra, String aula, int postiDisponibili, String vincolo){
        for (Appello a : appelli){
            if (a.getCodiceAppello().equals(codiceAppello)) {
                        a.setDataOra(dataOra);
                        a.setAula(aula);
                        a.setPostiDisponibili(postiDisponibili);
                        a.setVincoloLetteraCognome(vincolo);
                   
                        String oggetto = "Modifica appello : " + codiceAppello;
                        String contenuto = "L'appello " + codiceAppello + " è stato modificato.\n" + 
                                            "Orario: " + dataOra + "\n" +
                                            "Aula: " + aula+ "\n" + 
                                            "Posti disponibili" + postiDisponibili + "\n" +
                                            "Vincolo cognome" + vincolo + "\n";
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
}