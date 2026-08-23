package it.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.project.database.ClockProvider;
import it.project.observer.ObserverEsitoVoto;
import it.project.state.BocciatoState;
import it.project.state.IStatoVoto;
import it.project.state.InAttesaConfermaState;

/**
 * Rappresenta un esame sostenuto da uno studente con il relativo esito.
 * Applica lo State Pattern per gestire il ciclo di vita del voto
 * e l'Observer Pattern per notificare gli attori al cambio di stato.
 *
 * Regola di Dominio 4: se il voto è inferiore a 18, lo stato iniziale è
 * BocciatoState.
 */
public class EsameSostenuto {

    private final String idVerbale;
    private final String codiceAppello;
    private final String matricolaStudente;
    private final String codiceMateria;
    private final String idProfessore;
    private final int votoNumerico;
    private final boolean lode;
    private final int cfu;
    private IStatoVoto stato;
    private final LocalDateTime scadenzaConferma;
    private final LocalDateTime dataRegistrazione;

    // Observer
    private final List<ObserverEsitoVoto> osservatori;

    /**
     * Costruttore principale.
     * Applica automaticamente la Regola di Dominio 4: voto &lt; 18 -&gt; BocciatoState.
     *
     * @param idVerbale         identificativo univoco del verbale dell'esame
     *                          sostenuto
     * @param codiceAppello     codice dell'appello d'esame
     * @param matricolaStudente matricola dello studente
     * @param codiceMateria     codice della materia
     * @param idProfessore      identificativo del professore che ha pubblicato
     *                          l'esito
     * @param votoNumerico      voto numerico (0-30, dove 0 può indicare assente)
     * @param lode              true se il voto è 30 e lode
     * @param cfu               crediti formativi della materia
     * @param giorniScadenza    giorni di scadenza per la conferma (Estensione A)
     */
    public EsameSostenuto(String idVerbale, String codiceAppello, String matricolaStudente,
            String codiceMateria, String idProfessore,
            int votoNumerico, boolean lode, int cfu, int giorniScadenza) {
        this.idVerbale = idVerbale;
        this.codiceAppello = codiceAppello;
        this.matricolaStudente = matricolaStudente;
        this.codiceMateria = codiceMateria;
        this.idProfessore = idProfessore;
        this.votoNumerico = votoNumerico;
        this.lode = lode;
        this.cfu = cfu;
        this.dataRegistrazione = ClockProvider.nowLocalDateTime();
        this.scadenzaConferma = this.dataRegistrazione.plusDays(giorniScadenza);
        this.osservatori = new ArrayList<>();

        // Regola di Dominio 4: voto insufficiente → stato Bocciato
        if (votoNumerico < 18) {
            this.stato = new BocciatoState();
        } else {
            this.stato = new InAttesaConfermaState();
        }
    }

    // =========================================================================
    // STATE PATTERN: Delegazione allo stato corrente
    // =========================================================================

    /**
     * Lo studente accetta il voto. Delega allo stato corrente e notifica gli
     * osservatori.
     */
    public void accetta() {
        stato.accetta(this);
        notificaOsservatori();
    }

    /**
     * Lo studente rifiuta il voto. Delega allo stato corrente e notifica gli
     * osservatori.
     */
    public void rifiuta() {
        stato.rifiuta(this);
        notificaOsservatori();
    }

    /**
     * Imposta il nuovo stato dell'esame (utilizzato dagli stati concreti per la transizione).
     *
     * @param nuovoStato il nuovo stato dell'esame
     */
    public void setStato(IStatoVoto nuovoStato) {
        this.stato = nuovoStato;
    }

    /**
     * Verifica se la scadenza per la conferma è passata (Estensione A - Silenzio Rifiuto).
     * 
     * @return true se la scadenza è superata e l'esame è ancora in attesa di conferma
     */
    public boolean isScaduto() {
        return stato.getNome().equals("In attesa di conferma")
                && ClockProvider.nowLocalDateTime().isAfter(scadenzaConferma);
    }

    // =========================================================================
    // OBSERVER PATTERN
    // =========================================================================

    /**
     * Registra un osservatore per ricevere aggiornamenti sui cambi di stato del voto.
     *
     * @param observer l'osservatore da registrare
     */
    public void aggiungiOsservatore(ObserverEsitoVoto observer) {
        osservatori.add(observer);
    }

    /**
     * Rimuove un osservatore registrato.
     *
     * @param observer l'osservatore da rimuovere
     */
    public void rimuoviOsservatore(ObserverEsitoVoto observer) {
        osservatori.remove(observer);
    }

    private void notificaOsservatori() {
        String nomeStato = stato.getNome();
        for (ObserverEsitoVoto obs : osservatori) {
            obs.aggiornamento(this, nomeStato);
        }
    }

    // =========================================================================
    // GETTER
    // =========================================================================

    /**
     * Restituisce l'identificativo del verbale d'esame.
     *
     * @return id verbale
     */
    public String getIdVerbale() {
        return idVerbale;
    }

    /**
     * Restituisce il codice dell'appello associato all'esame.
     *
     * @return codice appello
     */
    public String getCodiceAppello() {
        return codiceAppello;
    }

    /**
     * Restituisce la matricola dello studente.
     *
     * @return matricola studente
     */
    public String getMatricolaStudente() {
        return matricolaStudente;
    }

    /**
     * Restituisce il codice della materia dell'esame.
     *
     * @return codice materia
     */
    public String getCodiceMateria() {
        return codiceMateria;
    }

    /**
     * Restituisce l'identificativo del professore che ha verbalizzato l'esito.
     *
     * @return id professore
     */
    public String getIdProfessore() {
        return idProfessore;
    }

    /**
     * Restituisce il voto numerico conseguito (0-30).
     *
     * @return voto numerico
     */
    public int getVotoNumerico() {
        return votoNumerico;
    }

    /**
     * Indica se è stata attribuita la lode.
     *
     * @return true se 30 e lode, false altrimenti
     */
    public boolean isLode() {
        return lode;
    }

    /**
     * Restituisce il numero di CFU della materia verbalizzata.
     *
     * @return crediti formativi universitari
     */
    public int getCfu() {
        return cfu;
    }

    /**
     * Restituisce l'oggetto che rappresenta lo stato corrente del voto.
     *
     * @return istanza di IStatoVoto
     */
    public IStatoVoto getStato() {
        return stato;
    }

    /**
     * Restituisce la descrizione testuale dello stato corrente del voto.
     *
     * @return nome dello stato
     */
    public String getNomeStato() {
        return stato.getNome();
    }

    /**
     * Restituisce la data e ora limite per la conferma o rifiuto del voto.
     *
     * @return data di scadenza conferma
     */
    public LocalDateTime getScadenzaConferma() {
        return scadenzaConferma;
    }

    /**
     * Restituisce la data e ora di registrazione/pubblicazione dell'esito.
     *
     * @return data registrazione
     */
    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }

    @Override
    public String toString() {
        return "EsameSostenuto [idVerbale=" + idVerbale
                + ", materia=" + codiceMateria
                + ", studente=" + matricolaStudente
                + ", voto=" + votoNumerico + (lode ? "L" : "")
                + ", stato=" + stato.getNome()
                + ", scadenza=" + scadenzaConferma + "]";
    }
}
