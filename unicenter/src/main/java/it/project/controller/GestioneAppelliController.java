package it.project.controller;

import it.project.Appello;
import it.project.EsameSostenuto;
import it.project.Studente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import it.project.Notifica;
import it.project.Professore;
import it.project.Unicenter;
import it.project.database.ClockProvider;
import it.project.exceptions.AppelloNonTrovatoException;
import it.project.exceptions.DataNonValidaException;
import it.project.exceptions.PostiNonValidi;
import it.project.generator.CodiceAppelloGenerator;
import it.project.validation.*;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class GestioneAppelliController {
    private static final Pattern VINCOLO_PATTERN = Pattern.compile("^[A-Z]-[A-Z]$");
    private IscrizioneValidator validatorChain;
    private final List<Appello> appelli;
    private final CodiceAppelloGenerator codiceAppelloGenerator;
    Unicenter unicenter;

    public GestioneAppelliController(Unicenter unicenter) {
        this.unicenter = unicenter;
        this.validatorChain = ValidationChainBuilder.buildDefaultChain();
        this.appelli = new ArrayList<>();
        this.codiceAppelloGenerator = CodiceAppelloGenerator.getInstance();
    }

    private String normalizzaEValidaVincolo(String vincoloLetteraCognome) {
        if (vincoloLetteraCognome == null || vincoloLetteraCognome.trim().isEmpty()) {
            return "";
        }

        // Rimuove spazi e normalizza eventuali varianti del trattino (– — -)
        String pulito = vincoloLetteraCognome.trim()
                .replaceAll("\\s+", "")
                .replace('–', '-')
                .replace('—', '-');

        // Rimuove gli accenti: decompone il carattere (é -> e + accento e toglie i
        // segni diacritici (categoria Unicode "Mark")
        String senzaAccenti = Normalizer.normalize(pulito, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();

        if (!VINCOLO_PATTERN.matcher(senzaAccenti).matches()) {
            throw new IllegalArgumentException(
                    "Il vincolo lettera cognome deve essere nel formato 'A-Z' (una lettera, trattino, una lettera).");
        }

        char primaLettera = senzaAccenti.charAt(0);
        char secondaLettera = senzaAccenti.charAt(2);

        // Riporta all'ordine alfabetico correnotto se invertito (es. "Z-A" -> "A-Z")
        if (primaLettera > secondaLettera) {
            return "" + secondaLettera + "-" + primaLettera;
        }
        return senzaAccenti;
    }

    public boolean creaNuovoAppello(String codiceMateria, LocalDateTime dataOraStr, String aula, int postiDisponibili,
            String vincoloLetteraCognome, LocalDate termineIscrizione) throws Exception {

        String vincoloNormalizzato = normalizzaEValidaVincolo(vincoloLetteraCognome);
        validateAppello(codiceMateria, dataOraStr, aula, postiDisponibili, vincoloNormalizzato, termineIscrizione);

        String codiceAppello = codiceAppelloGenerator.generateCodice();
        Appello appello = new Appello(codiceAppello, codiceMateria, dataOraStr, aula, postiDisponibili,
                vincoloNormalizzato, termineIscrizione);
        appelli.add(appello);
        return true;
    }

    public void validateAppello(String codiceMateria, LocalDateTime dataOraStr, String aula,
            int postiDisponibili, String vincoloLetteraCognome, LocalDate termineIscrizione)
            throws Exception, DataNonValidaException, PostiNonValidi {

        if (dataOraStr == null || dataOraStr.isBefore(ClockProvider.nowLocalDateTime())) {
            throw new DataNonValidaException("La data e l'ora dell'appello non sono valide.");
        }

        if (termineIscrizione == null || !termineIscrizione.isBefore(dataOraStr.toLocalDate())
                || termineIscrizione.isBefore(ClockProvider.nowLocalDate())) {
            throw new DataNonValidaException(
                    "La data di termine iscrizione non è valida: deve essere compresa tra oggi e al massimo il giorno prima dell'appello.");
        }

        if (postiDisponibili <= 0) {
            throw new PostiNonValidi("Il numero di posti disponibili deve essere maggiore di zero.");
        }

        // if (!(unicenter.getCurrentUser() instanceof Professore)) {
        // throw new IllegalArgumentException("Solo un professore autenticato può
        // gestire gli appelli.");
        // }

        if (unicenter.getCurrentUser() != null && !unicenter.isProfessoreAbilitatoAMateria(codiceMateria)) {
            throw new IllegalArgumentException("Il professore non è abilitato a gestire questa materia.");
        }
        return;
    }

    public boolean iscriviStudente(Studente studente, String codiceAppello) throws Exception {
        Appello appello = trovaAppelloByIdAppello(codiceAppello);

        if (this.validatorChain == null) {
            this.validatorChain = ValidationChainBuilder.buildDefaultChain();
        }

        if (appello.getIscritti().contains(studente)) {
            throw new IllegalStateException("Sei già iscritto a questo appello.");
        }

        // Controlla se lo studente ha già superato l'esame per questa materia
        String codiceMateria = appello.getCodiceMateria();
        if (studente.getLibretto() != null && studente.getLibretto().isEsameSuperato(codiceMateria)) {
            throw new IllegalStateException(
                    "Iscrizione rifiutata: esame già superato e verbalizzato nel libretto.");
        }

        // Controlla se lo studente ha un esito pendente per questa materia
        List<EsameSostenuto> esitiPendenti = unicenter.getEsitiPendentiByMatricola(studente.getMatricola());
        if (esitiPendenti != null) {
            for (EsameSostenuto esame : esitiPendenti) {
                if (esame.getCodiceMateria().equals(codiceMateria)) {
                    throw new IllegalStateException(
                            "Hai un esito pendente di questa materia. Non puoi prenotarti ad un altro appello.");
                }
            }
        }

        // Esegue i controlli della Chain of Responsibility (lancia eccezione in caso di
        // violazione)
        validatorChain.validate(studente, appello);

        // Se la validazione passa, registra l'iscritto
        appello.aggiungiIscritto(studente);
        String messaggio = "Ti sei iscritto all'appello: " + appello.toString();
        Notifica nuovaNotifica = new Notifica("Iscrizione Appello", messaggio, ClockProvider.nowLocalDateTime());

        // Invia la notifica allo studente
        studente.riceviNotifica(nuovaNotifica);

        return true;
    }

    public boolean disiscriviStudente(Studente studente, String codiceAppello) throws Exception {
        Appello appello = trovaAppelloByIdAppello(codiceAppello);

        // Vincolo temporale: la disiscrizione è bloccata dopo la data di termine
        // iscrizione
        if (appello.getTermineIscrizione() != null
                && ClockProvider.nowLocalDate().isAfter(appello.getTermineIscrizione())) {
            throw new IllegalStateException(
                    "Impossibile annullare la prenotazione: il termine di iscrizione ("
                            + appello.getTermineIscrizione() + ") è scaduto.");
        }

        // Vincolo: la disiscrizione è bloccata se lo studente ha un esito pendente per
        // questo appello
        List<EsameSostenuto> esitiPendenti = unicenter.getEsitiPendentiByMatricola(studente.getMatricola());
        if (esitiPendenti != null && !esitiPendenti.isEmpty()) {
            for (EsameSostenuto esame : esitiPendenti) {
                if (esame.getCodiceAppello().equals(codiceAppello)) {
                    throw new IllegalStateException(
                            "Impossibile annullare la prenotazione: hai un esito pendente per questo appello. Devi prima accettare o rifiutare il voto.");
                }
            }
        }

        if (appello.getIscritti().contains(studente)) {
            appello.rimuoviIscritto(studente);
            String messaggio = "Ti sei disiscritto dall'appello: " + appello.toString();
            Notifica nuovaNotifica = new Notifica("[Disiscrizione Appello]", messaggio,
                    ClockProvider.nowLocalDateTime());
            studente.riceviNotifica(nuovaNotifica);
            return true;
        } else {
            throw new IllegalStateException("Non sei iscritto a questo appello.");
        }
    }

    public List<Appello> trovaAppelliByIdMateria(List<String> codiciMaterie) {
        if (appelli == null || appelli.isEmpty()) {
            return Collections.emptyList(); // Nessun appello disponibile
        }
        if (codiciMaterie == null || codiciMaterie.isEmpty()) {
            return Collections.emptyList();
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

    public List<Appello> trovaAppelliPrenotabiliByStudente(Studente studente, List<String> codiciMaterie) {
        if (studente == null) {
            return Collections.emptyList();
        }
        List<Appello> appelliById = trovaAppelliByIdMateria(codiciMaterie);
        List<Appello> appelliPrenotabili = new ArrayList<>();
        if (appelliById == null || appelliById.isEmpty()) {
            return Collections.emptyList(); // Nessun appello disponibile per le materie specificate
        }

        for (Appello app : appelliById) {
            String codiceMateria = app.getCodiceMateria();

            // Escludi appelli di materie già superate e registrate nel libretto
            if (studente.getLibretto() != null && studente.getLibretto().isEsameSuperato(codiceMateria)) {
                continue;
            }

            // Escludi appelli a cui lo studente è già iscritto
            if (app.getIscritti().contains(studente)) {
                continue;
            }

            appelliPrenotabili.add(app);
        }
        return appelliPrenotabili;
    }

    public Appello trovaAppelloByIdAppello(String codiceAppello) {
        if (appelli != null && codiceAppello != null) {
            for (Appello app : appelli) {
                if (app.getCodiceAppello().equals(codiceAppello)) {
                    return app;
                }
            }
        }
        throw new AppelloNonTrovatoException("Appello non trovato: " + codiceAppello);
    }

    public String generaCodiceAppello() {
        return codiceAppelloGenerator.generateCodice();
    }

    public List<Studente> trovaIscrittiByIdAppello(String codiceAppello) {
        Appello appello = trovaAppelloByIdAppello(codiceAppello);
        return appello.getIscritti();
    }

    public boolean modificaAppello(String codiceAppello, LocalDateTime dataOra, String aula, int postiDisponibili,
            String vincolo, LocalDate dataTermineIscrizione) throws Exception, DataNonValidaException, PostiNonValidi {

        Appello appello = trovaAppelloByIdAppello(codiceAppello);
        String vincoloNormalizzato = normalizzaEValidaVincolo(vincolo);
        validateAppello(appello.getCodiceMateria(), dataOra, aula, postiDisponibili, vincoloNormalizzato,
                dataTermineIscrizione);

        int iscrittiPresenti = appello.getIscritti().size();
        if (postiDisponibili < iscrittiPresenti) {
            throw new PostiNonValidi(
                    "Il numero di posti disponibili non può essere inferiore agli iscritti già presenti ("
                            + iscrittiPresenti + ").");
        }

        int postiRimanenti = postiDisponibili - iscrittiPresenti;

        appello.setDataOra(dataOra);
        appello.setAula(aula);
        appello.setPostiDisponibili(postiRimanenti);
        appello.setVincoloLetteraCognome(vincoloNormalizzato);
        appello.setTermineIscrizione(dataTermineIscrizione);

        String oggetto = "Modifica appello : " + codiceAppello;
        String contenuto = "L'appello " + codiceAppello + " è stato modificato.\n" +
                "Orario: " + dataOra + "\n" +
                "Aula: " + aula + "\n" +
                "Posti disponibili " + postiRimanenti + "\n" +
                "Vincolo cognome " + vincoloNormalizzato + "\n" +
                "Data termine iscrizione: " + dataTermineIscrizione + "\n";
        Notifica notifica = new Notifica(oggetto, contenuto, LocalDateTime.now());
        appello.notifica(notifica);
        return true;
    }

    public boolean eliminaAppello(String codiceAppello) {
        Appello appello = trovaAppelloByIdAppello(codiceAppello);

        String oggetto = "Eliminazione appello " + codiceAppello;
        String contenuto = "L'appello " + codiceAppello + " è stato eliminato.";
        Notifica notifica = new Notifica(oggetto, contenuto, LocalDateTime.now());
        appello.notifica(notifica);

        appelli.remove(appello);
        return true;
    }

    public List<Appello> appelliPrenotatiByStudente(Studente studente) {
        List<Appello> appelliPrenotati = new ArrayList<>();
        if (appelli == null || appelli.isEmpty() || studente == null) {
            return Collections.emptyList(); // Nessun appello disponibile
        }
        for (Appello appello : appelli) {
            if (appello.getIscritti().contains(studente)) {
                appelliPrenotati.add(appello);
            }
        }
        return appelliPrenotati;
    }
}