package it.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.project.database.ClockProvider;
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

    /**
     * Costruttore di default.
     * Inizializza il piano in stato {@link it.project.state.StatoBozzaPiano} con liste vuote.
     */
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
     * Rimuove le materie a scelta rifiutate.
     */
    public void rifiuta() {
        rifiuta(Collections.emptyList());
    }

    /**
     * Rifiuto da parte dell'amministratore con mantenimento delle sole materie verbalizzate.
     *
     * @param codiciMaterieVerbalizzate lista delle materie a scelta già superate e registrate
     */
    public void rifiuta(List<String> codiciMaterieVerbalizzate) {
        stato.rifiuta(this);
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

    /**
     * Imposta lo stato del piano di studi tramite oggetto stato.
     *
     * @param nuovoStato nuovo stato
     */
    public void setStato(IStatoPianoDiStudi nuovoStato) {
        this.stato = nuovoStato;
    }

    /**
     * Imposta lo stato del piano di studi tramite stringa identificativa.
     *
     * @param nomeStato nome dello stato (BOZZA, IN_ATTESA, APPROVATO, REGISTRATO, RIFIUTATO)
     */
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

    /**
     * Restituisce l'oggetto stato corrente del piano.
     *
     * @return istanza di {@link IStatoPianoDiStudi}
     */
    public IStatoPianoDiStudi getStatoCorrente() {
        return stato;
    }

    /**
     * Restituisce il nome descrittivo dello stato del piano.
     *
     * @return nome dello stato
     */
    public String getNomeStato() {
        return stato != null ? stato.getNome() : "N/D";
    }

    /**
     * Retrocompatibilità con codice esistente che legge getStato() come String.
     *
     * @return nome dello stato
     */
    public String getStato() {
        return getNomeStato();
    }

    /**
     * Verifica se il piano di studi è approvato o registrato.
     *
     * @return true se approvato o registrato, false altrimenti
     */
    public boolean isApprovato() {
        return stato != null && stato.isApprovato();
    }

    // =========================================================================
    // GESTIONE MATERIE OBBLIGATORIE
    // =========================================================================

    /**
     * Aggiunge una materia obbligatoria al piano di studi.
     *
     * @param codiceMateria codice della materia
     */
    public void aggiungiMateriaObbligatoria(String codiceMateria) {
        if (codiceMateria != null && !idMaterieObbligatorie.contains(codiceMateria)) {
            idMaterieObbligatorie.add(codiceMateria);
        }
    }

    /**
     * Restituisce la lista immutabile delle materie obbligatorie.
     *
     * @return lista codici materie obbligatorie
     */
    public List<String> getIdMaterieObbligatorie() {
        return Collections.unmodifiableList(idMaterieObbligatorie);
    }

    /**
     * Verifica se una materia fa parte delle materie obbligatorie.
     *
     * @param codiceMateria codice della materia
     * @return true se obbligatoria
     */
    public boolean isMateriaObbligatoria(String codiceMateria) {
        return idMaterieObbligatorie.contains(codiceMateria);
    }

    // =========================================================================
    // GESTIONE MATERIE A SCELTA
    // =========================================================================

    /**
     * Aggiunge una materia a scelta al piano di studi.
     *
     * @param codiceMateria codice della materia a scelta
     */
    public void aggiungiMateriaAScelta(String codiceMateria) {
        if (codiceMateria != null && !idMaterieAScelta.contains(codiceMateria)
                && !idMaterieObbligatorie.contains(codiceMateria)) {
            idMaterieAScelta.add(codiceMateria);
        }
    }

    /**
     * Rimuove una materia a scelta dal piano.
     *
     * @param codiceMateria codice della materia da rimuovere
     */
    public void rimuoviMateriaAScelta(String codiceMateria) {
        if (codiceMateria != null) {
            idMaterieAScelta.remove(codiceMateria);
        }
    }

    /**
     * Restituisce la lista immutabile delle materie a scelta selezionate.
     *
     * @return lista codici materie a scelta
     */
    public List<String> getIdMaterieAScelta() {
        return Collections.unmodifiableList(idMaterieAScelta);
    }

    /**
     * Verifica se una materia fa parte delle materie a scelta nel piano.
     *
     * @param codiceMateria codice della materia
     * @return true se a scelta
     */
    public boolean isMateriaAScelta(String codiceMateria) {
        return idMaterieAScelta.contains(codiceMateria);
    }

    // =========================================================================
    // METODI DI RETROCOMPATIBILITÀ E UTILITY
    // =========================================================================

    /**
     * Aggiunge una materia generica (aggiunta come obbligatoria per default se non specificato).
     *
     * @param codiceMateria codice della materia
     */
    public void aggiungiMateria(String codiceMateria) {
        aggiungiMateriaObbligatoria(codiceMateria);
    }

    /**
     * Verifica se il piano contiene una data materia (obbligatoria o a scelta).
     *
     * @param codiceMateria codice della materia
     * @return true se contenuta nel piano
     */
    public boolean contieneMateria(String codiceMateria) {
        return idMaterieObbligatorie.contains(codiceMateria) || idMaterieAScelta.contains(codiceMateria);
    }

    /**
     * Restituisce l'unione di tutte le materie (obbligatorie + a scelta).
     *
     * @return lista immutabile di tutti i codici materia
     */
    public List<String> getIdMaterie() {
        List<String> tutte = new ArrayList<>(idMaterieObbligatorie);
        tutte.addAll(idMaterieAScelta);
        return Collections.unmodifiableList(tutte);
    }

    // =========================================================================
    // OBSERVER PATTERN: Subject / Observable per Notifiche Piano di Studi (UC9)
    // =========================================================================

    /**
     * Aggiunge un osservatore alle notifiche del piano di studi.
     *
     * @param obs osservatore da registrare
     */
    public void aggiungiOsservatore(ObserverNotifica obs) {
        if (obs != null && !osservatori.contains(obs)) {
            osservatori.add(obs);
        }
    }

    /**
     * Rimuove un osservatore dalle notifiche del piano di studi.
     *
     * @param obs osservatore da rimuovere
     */
    public void rimuoviOsservatore(ObserverNotifica obs) {
        if (obs != null) {
            osservatori.remove(obs);
        }
    }

    private void notificaOsservatori(String esito) {
        String oggetto = "Esito Piano di Studi: " + esito;
        String messaggio = "Il tuo piano di studi è stato " + esito.toLowerCase() + " dalla Segreteria/Amministratore.";
        Notifica notifica = new Notifica(oggetto, messaggio, ClockProvider.nowLocalDateTime());
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