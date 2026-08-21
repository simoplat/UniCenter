package it.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.project.observer.ObserverNotifica;
import it.project.state.IStatoPianoDiStudi;
import it.project.state.StatoBozzaPiano;

/**
 * Piano di Studi dello Studente (UC9).
 * Contiene le materie obbligatorie (ereditate dal CorsoDiLaurea) e le materie
 * a scelta selezionate dallo studente. Applica lo State Pattern per gestire il
 * ciclo di vita dell'approvazione e l'Observer Pattern per notificare lo studente.
 *
 * Vincoli:
 * - Minimo 12 CFU di materie a scelta, nessun limite massimo
 * - Lo studente può ri-compilare il piano da qualsiasi stato (Approvato, Registrato, Rifiutato)
 *   purché non abbia appelli prenotati o esiti pendenti per materie a scelta
 * - Le materie a scelta già verbalizzate nel Libretto NON possono essere rimosse
 */
public class PianoDiStudi {
    private IStatoPianoDiStudi stato;
    private final List<String> idMaterieObbligatorie;
    private final List<String> idMaterieAScelta;
    private final List<ObserverNotifica> osservatori;

    public PianoDiStudi() {
        this.stato = new StatoBozzaPiano();
        this.idMaterieObbligatorie = new ArrayList<>();
        this.idMaterieAScelta = new ArrayList<>();
        this.osservatori = new ArrayList<>();
    }

    // =========================================================================
    // STATE PATTERN TRANSITIONS
    // =========================================================================

    /**
     * Auto-approvazione del piano (tutte le materie a scelta sono pre-approvate).
     */
    public void registra() {
        stato.registra(this);
    }

    /**
     * Invia la richiesta di approvazione manuale all'amministratore.
     */
    public void richiediApprovazione() {
        stato.richiediApprovazione(this);
    }

    /**
     * Approvazione manuale da parte dell'amministratore e notifica osservatori.
     */
    public void approva() {
        stato.approva(this);
        notificaOsservatori("Approvato");
    }

    /**
     * Rifiuto da parte dell'amministratore e notifica osservatori.
     */
    public void rifiuta() {
        stato.rifiuta(this);
        notificaOsservatori("Rifiutato");
    }

    /**
     * Ri-compilazione del piano di studi.
     * Rimuove solo le materie a scelta NON verbalizzate.
     * Le materie verbalizzate restano fissate nel piano.
     *
     * Precondizione: il PianoStudiController ha già verificato che lo studente
     * non ha appelli prenotati né esiti pendenti per le materie a scelta.
     *
     * @param codiciMaterieVerbalizzate lista delle materie a scelta già superate e registrate
     */
    public void ricompila(List<String> codiciMaterieVerbalizzate) {
        stato.ricompila(this);
        List<String> materieBloccate = new ArrayList<>();
        if (codiciMaterieVerbalizzate != null) {
            for (String codice : idMaterieAScelta) {
                if (codiciMaterieVerbalizzate.contains(codice)) {
                    materieBloccate.add(codice);
                }
            }
        }
        idMaterieAScelta.clear();
        idMaterieAScelta.addAll(materieBloccate);
    }

    public void setStato(IStatoPianoDiStudi nuovoStato) {
        this.stato = nuovoStato;
    }

    public void setStato(String nomeStato) {
        if (nomeStato == null) return;
        switch (nomeStato.toUpperCase().replace(" ", "_")) {
            case "IN_ATTESA" -> this.stato = new it.project.state.StatoInAttesaPiano();
            case "APPROVATO" -> this.stato = new it.project.state.StatoApprovatoPiano();
            case "REGISTRATO" -> this.stato = new it.project.state.StatoRegistratoPiano();
            case "RIFIUTATO" -> this.stato = new it.project.state.StatoRifiutatoPiano();
            case "BOZZA" -> this.stato = new it.project.state.StatoBozzaPiano();
            default -> {}
        }
    }

    public IStatoPianoDiStudi getStatoCorrente() {
        return stato;
    }

    public String getNomeStato() {
        return stato != null ? stato.getNome() : "N/D";
    }

    /**
     * Retrocompatibilità con codice esistente che legge getStato() come String.
     */
    public String getStato() {
        return getNomeStato();
    }

    public boolean isApprovato() {
        return stato != null && stato.isApprovato();
    }

    // =========================================================================
    // GESTIONE MATERIE OBBLIGATORIE
    // =========================================================================

    public void aggiungiMateriaObbligatoria(String codiceMateria) {
        if (codiceMateria != null && !idMaterieObbligatorie.contains(codiceMateria)) {
            idMaterieObbligatorie.add(codiceMateria);
        }
    }

    public List<String> getIdMaterieObbligatorie() {
        return Collections.unmodifiableList(idMaterieObbligatorie);
    }

    public boolean isMateriaObbligatoria(String codiceMateria) {
        return idMaterieObbligatorie.contains(codiceMateria);
    }

    // =========================================================================
    // GESTIONE MATERIE A SCELTA
    // =========================================================================

    public void aggiungiMateriaAScelta(String codiceMateria) {
        if (codiceMateria != null && !idMaterieAScelta.contains(codiceMateria)
                && !idMaterieObbligatorie.contains(codiceMateria)) {
            idMaterieAScelta.add(codiceMateria);
        }
    }

    public void rimuoviMateriaAScelta(String codiceMateria) {
        if (codiceMateria != null) {
            idMaterieAScelta.remove(codiceMateria);
        }
    }

    public List<String> getIdMaterieAScelta() {
        return Collections.unmodifiableList(idMaterieAScelta);
    }

    public boolean isMateriaAScelta(String codiceMateria) {
        return idMaterieAScelta.contains(codiceMateria);
    }

    // =========================================================================
    // METODI DI RETROCOMPATIBILITÀ E UTILITY
    // =========================================================================

    /**
     * Aggiunge una materia generica (aggiunta come obbligatoria per default se non specificato).
     */
    public void aggiungiMateria(String codiceMateria) {
        aggiungiMateriaObbligatoria(codiceMateria);
    }

    public boolean contieneMateria(String codiceMateria) {
        return idMaterieObbligatorie.contains(codiceMateria) || idMaterieAScelta.contains(codiceMateria);
    }

    /**
     * Restituisce l'unione di tutte le materie (obbligatorie + a scelta).
     */
    public List<String> getIdMaterie() {
        List<String> tutte = new ArrayList<>(idMaterieObbligatorie);
        tutte.addAll(idMaterieAScelta);
        return Collections.unmodifiableList(tutte);
    }

    // =========================================================================
    // OBSERVER PATTERN: Subject / Observable per Notifiche Piano di Studi (UC9)
    // =========================================================================

    public void aggiungiOsservatore(ObserverNotifica obs) {
        if (obs != null && !osservatori.contains(obs)) {
            osservatori.add(obs);
        }
    }

    public void rimuoviOsservatore(ObserverNotifica obs) {
        if (obs != null) {
            osservatori.remove(obs);
        }
    }

    private void notificaOsservatori(String esito) {
        String oggetto = "Esito Piano di Studi: " + esito;
        String messaggio = "Il tuo piano di studi è stato " + esito.toLowerCase() + " dalla Segreteria/Amministratore.";
        Notifica notifica = new Notifica(oggetto, messaggio, LocalDateTime.now());
        for (ObserverNotifica obs : new ArrayList<>(osservatori)) {
            obs.riceviNotifica(notifica);
        }
    }

    @Override
    public String toString() {
        return "PianoDiStudi [stato=" + getNomeStato()
                + ", materieObbligatorie=" + idMaterieObbligatorie.size()
                + ", materieAScelta=" + idMaterieAScelta.size() + "]";
    }
}