package it.project.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.EsameSostenuto;
import it.project.Libretto;
import it.project.Materia;
import it.project.PianoDiStudi;
import it.project.Studente;
import it.project.Unicenter;
import it.project.exceptions.CorsoDiLaureaNonTrovatoException;
import it.project.strategy.ApprovazioneAutomatica;
import it.project.strategy.ApprovazioneManuale;
import it.project.strategy.PoliticaApprovazione;

/**
 * Facade Controller (GRASP) per UC9 - Compilazione Piano di Studi.
 * Coordina l'inserimento delle materie a scelta e smista la richiesta
 * verso la logica di validazione automatica o verso il flusso di
 * approvazione manuale.
 *
 * Vincolo di compilazione: la compilazione/ri-compilazione è bloccata se lo
 * studente ha un appello prenotato o un esito pendente per una qualsiasi
 * materia a scelta attualmente nel piano. Questo vincolo evita che lo studente
 * modifichi il piano mentre ha impegni attivi sulle materie a scelta.
 */
public class PianoStudiController {
    private final GestioneCorsiLaureaController gestioneCorsi;
    private final GestoreMaterieController gestoreMaterie;
    private final GestioneAppelliController gestioneAppelli;
    private final Unicenter unicenter;
    private final Map<String, PianoDiStudi> pianiInAttesa; // matricola -> piano

    public static final int MIN_CFU_A_SCELTA = 12;

    public PianoStudiController(GestioneCorsiLaureaController gestioneCorsi,
                                GestoreMaterieController gestoreMaterie,
                                GestioneAppelliController gestioneAppelli,
                                Unicenter unicenter) {
        this.gestioneCorsi = gestioneCorsi;
        this.gestoreMaterie = gestoreMaterie;
        this.gestioneAppelli = gestioneAppelli;
        this.unicenter = unicenter;
        this.pianiInAttesa = new HashMap<>();
    }

    /**
     * Compila il piano di studi: aggiunge le materie a scelta, verifica
     * il minimo CFU (12), determina la strategia di approvazione e la applica.
     *
     * @param studente lo studente che compila il piano
     * @param codiciMaterieAScelta i codici delle materie a scelta selezionate
     * @return true se la compilazione è avvenuta con successo
     * @throws IllegalStateException se lo studente ha appelli prenotati o esiti
     *         pendenti per materie a scelta attualmente nel piano
     * @throws IllegalArgumentException se i CFU sono insufficienti (< 12) o materie non valide
     */
    public boolean compilaPianoDiStudi(Studente studente, List<String> codiciMaterieAScelta) {
        if (studente == null) {
            throw new IllegalArgumentException("Studente non valido.");
        }
        PianoDiStudi piano = studente.getPianoDiStudi();
        if (piano == null) {
            throw new IllegalStateException("Lo studente non possiede un piano di studi associato.");
        }

        // 1. Verifica vincolo se non è la primissima compilazione (se ci sono già materie a scelta)
        if (!piano.getIdMaterieAScelta().isEmpty()) {
            verificaVincoloCompilazioneMaterie(studente);
        }

        // 2. Se è ri-compilazione (piano già approvato, registrato o rifiutato), torna in bozza
        //    mantenendo le materie a scelta già verbalizzate
        List<String> materieVerbalizzate = getMaterieASceltaVerbalizzate(studente);
        if (!"Bozza".equalsIgnoreCase(piano.getNomeStato())) {
            piano.ricompila(materieVerbalizzate);
        } else {
            // Se era in bozza, pulisci e mantieni solo le verbalizzate
            List<String> daRimuovere = new ArrayList<>(piano.getIdMaterieAScelta());
            for (String m : daRimuovere) {
                if (!materieVerbalizzate.contains(m)) {
                    piano.rimuoviMateriaAScelta(m);
                }
            }
        }

        // 3. Aggiungi le materie selezionate
        List<Materia> materieScelte = new ArrayList<>();
        if (codiciMaterieAScelta != null) {
            for (String cod : codiciMaterieAScelta) {
                Materia m = gestoreMaterie.trovaMaterieByCodice(cod);
                if (m == null) {
                    throw new IllegalArgumentException("Materia con codice '" + cod + "' non trovata nel sistema.");
                }
                if (piano.isMateriaObbligatoria(cod)) {
                    throw new IllegalArgumentException("La materia '" + m.getNome() + "' è già obbligatoria nel tuo piano di studi.");
                }
                piano.aggiungiMateriaAScelta(cod);
            }
        }

        // 4. Calcola totale CFU di tutte le materie a scelta (comprese verbalizzate)
        int totaleCfuAScelta = 0;
        for (String cod : piano.getIdMaterieAScelta()) {
            Materia m = gestoreMaterie.trovaMaterieByCodice(cod);
            if (m != null) {
                materieScelte.add(m);
                totaleCfuAScelta += m.getCfu();
            }
        }

        if (totaleCfuAScelta < MIN_CFU_A_SCELTA) {
            throw new IllegalArgumentException("CFU a scelta insufficienti: " + totaleCfuAScelta
                    + " CFU selezionati. Il minimo richiesto è " + MIN_CFU_A_SCELTA + " CFU.");
        }

        // 5. Determina corso di laurea dello studente
        CorsoDiLaurea corso = null;
        try {
            corso = gestioneCorsi.trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());
        } catch (CorsoDiLaureaNonTrovatoException e) {
            try {
                corso = gestioneCorsi.trovaCorsoDiLaureaByNome(studente.getIdCorsoDiLaurea());
            } catch (CorsoDiLaureaNonTrovatoException ex) {
                // corso non trovato
            }
        }

        // 6. Verifica se tutte le materie a scelta sono pre-approvate
        boolean tuttePreApprovate = (corso != null) && corso.tuttePreApprovate(materieScelte);

        // 7. Seleziona ed applica la Strategy
        PoliticaApprovazione politica;
        if (tuttePreApprovate) {
            politica = new ApprovazioneAutomatica();
            politica.applica(piano);
            pianiInAttesa.remove(studente.getMatricola());
        } else {
            politica = new ApprovazioneManuale();
            politica.applica(piano);
            pianiInAttesa.put(studente.getMatricola(), piano);
        }

        return true;
    }

    /**
     * Verifica che lo studente non abbia appelli prenotati o esiti pendenti
     * per le materie a scelta attualmente nel piano.
     *
     * Controlla:
     * - Appelli prenotati: incrocia gli appelli prenotati dallo studente
     *   (via GestioneAppelliController.appelliPrenotatiByStudente) con le
     *   materie a scelta del piano
     * - Esiti pendenti: incrocia gli esiti "In attesa di conferma"
     *   (via Unicenter.getEsitiPendentiByMatricola) con le materie a scelta
     *
     * @throws IllegalStateException se esiste almeno un conflitto
     */
    public void verificaVincoloCompilazioneMaterie(Studente studente) {
        PianoDiStudi piano = studente.getPianoDiStudi();
        if (piano == null) return;
        List<String> materieAScelta = piano.getIdMaterieAScelta();

        if (materieAScelta.isEmpty()) return; // prima compilazione, nessun vincolo

        // 1. Controlla appelli prenotati per materie a scelta
        List<Appello> appelliPrenotati = gestioneAppelli.appelliPrenotatiByStudente(studente);
        if (appelliPrenotati != null) {
            for (Appello appello : appelliPrenotati) {
                if (materieAScelta.contains(appello.getCodiceMateria())) {
                    throw new IllegalStateException(
                        "Impossibile ri-compilare il piano: hai un appello prenotato per la materia '"
                        + appello.getCodiceMateria() + "' (appello: " + appello.getCodiceAppello()
                        + "). Disiscriviti prima dall'appello.");
                }
            }
        }

        // 2. Controlla esiti pendenti per materie a scelta
        if (unicenter != null) {
            List<EsameSostenuto> esitiPendenti = unicenter.getEsitiPendentiByMatricola(studente.getMatricola());
            if (esitiPendenti != null) {
                for (EsameSostenuto esame : esitiPendenti) {
                    if (materieAScelta.contains(esame.getCodiceMateria())) {
                        throw new IllegalStateException(
                            "Impossibile ri-compilare il piano: hai un esito pendente per la materia '"
                            + esame.getCodiceMateria() + "'. Accetta o rifiuta prima il voto.");
                    }
                }
            }
        }
    }

    /**
     * Restituisce i codici delle materie a scelta già verbalizzate dallo studente.
     * Queste materie NON possono essere rimosse durante la ri-compilazione.
     */
    public List<String> getMaterieASceltaVerbalizzate(Studente studente) {
        if (studente == null) return Collections.emptyList();
        Libretto libretto = studente.getLibretto();
        PianoDiStudi piano = studente.getPianoDiStudi();
        if (libretto == null || piano == null) return Collections.emptyList();

        List<String> verbalizzate = new ArrayList<>();
        for (String codice : piano.getIdMaterieAScelta()) {
            if (libretto.isEsameSuperato(codice)) {
                verbalizzate.add(codice);
            }
        }
        return verbalizzate;
    }

    /**
     * L'amministratore approva un piano di studi in attesa.
     *
     * @param matricolaStudente la matricola dello studente
     * @return true se approvato con successo
     */
    public boolean approvaPianoDiStudi(String matricolaStudente) {
        PianoDiStudi piano = pianiInAttesa.get(matricolaStudente);
        if (piano == null) {
            // Cerca tra tutti gli utenti nel sistema
            if (unicenter != null) {
                Studente st = unicenter.trovaStudenteByMatricola(matricolaStudente);
                if (st != null && st.getPianoDiStudi() != null && "In Attesa".equalsIgnoreCase(st.getPianoDiStudi().getNomeStato())) {
                    piano = st.getPianoDiStudi();
                }
            }
        }
        if (piano == null) {
            throw new IllegalArgumentException("Nessun piano in attesa trovato per la matricola: " + matricolaStudente);
        }

        piano.approva();
        pianiInAttesa.remove(matricolaStudente);
        return true;
    }

    /**
     * L'amministratore rifiuta un piano di studi in attesa.
     *
     * @param matricolaStudente la matricola dello studente
     * @return true se rifiutato con successo
     */
    public boolean rifiutaPianoDiStudi(String matricolaStudente) {
        PianoDiStudi piano = pianiInAttesa.get(matricolaStudente);
        if (piano == null) {
            if (unicenter != null) {
                Studente st = unicenter.trovaStudenteByMatricola(matricolaStudente);
                if (st != null && st.getPianoDiStudi() != null && "In Attesa".equalsIgnoreCase(st.getPianoDiStudi().getNomeStato())) {
                    piano = st.getPianoDiStudi();
                }
            }
        }
        if (piano == null) {
            throw new IllegalArgumentException("Nessun piano in attesa trovato per la matricola: " + matricolaStudente);
        }

        piano.rifiuta();
        pianiInAttesa.remove(matricolaStudente);
        return true;
    }

    /**
     * Restituisce la mappa dei piani in attesa di approvazione (matricola -> PianoDiStudi).
     */
    public Map<String, PianoDiStudi> getPianiInAttesa() {
        return Collections.unmodifiableMap(pianiInAttesa);
    }

    /**
     * L'amministratore aggiunge una materia pre-approvata a un corso.
     */
    public void aggiungiMateriaPreApprovata(String codiceCorso, Materia materia) {
        CorsoDiLaurea corso = gestioneCorsi.trovaCorsoDiLaureaById(codiceCorso);
        corso.aggiungiMateriaPreApprovata(materia);
    }

    /**
     * L'amministratore rimuove una materia pre-approvata da un corso.
     */
    public void rimuoviMateriaPreApprovata(String codiceCorso, Materia materia) {
        CorsoDiLaurea corso = gestioneCorsi.trovaCorsoDiLaureaById(codiceCorso);
        corso.rimuoviMateriaPreApprovata(materia);
    }

    /**
     * Restituisce le materie a scelta disponibili per uno studente
     * (tutte le materie del sistema che NON appartengono al manifesto obbligatorio del suo corso).
     */
    public List<Materia> getMaterieASceltaDisponibili(Studente studente) {
        if (studente == null) return Collections.emptyList();
        CorsoDiLaurea corso = null;
        try {
            corso = gestioneCorsi.trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());
        } catch (CorsoDiLaureaNonTrovatoException e) {
            try {
                corso = gestioneCorsi.trovaCorsoDiLaureaByNome(studente.getIdCorsoDiLaurea());
            } catch (CorsoDiLaureaNonTrovatoException ex) {
                // corso non trovato
            }
        }

        List<Materia> tutte = gestoreMaterie.getTutteLeMaterie();
        List<Materia> disponibili = new ArrayList<>();

        List<String> codiciManifesto = new ArrayList<>();
        if (corso != null) {
            for (Materia m : corso.getMaterie()) {
                codiciManifesto.add(m.getCodiceMateria());
            }
        }

        for (Materia m : tutte) {
            if (!codiciManifesto.contains(m.getCodiceMateria())) {
                disponibili.add(m);
            }
        }
        return disponibili;
    }

    /**
     * Restituisce le materie pre-approvate per un corso di laurea.
     */
    public List<Materia> getMateriePreApprovateByCorso(String codiceCorso) {
        CorsoDiLaurea corso = null;
        try {
            corso = gestioneCorsi.trovaCorsoDiLaureaById(codiceCorso);
        } catch (CorsoDiLaureaNonTrovatoException e) {
            try {
                corso = gestioneCorsi.trovaCorsoDiLaureaByNome(codiceCorso);
            } catch (CorsoDiLaureaNonTrovatoException ex) {
                // corso non trovato
            }
        }
        if (corso == null) {
            return Collections.emptyList();
        }
        return corso.getMateriePreApprovate();
    }
}
