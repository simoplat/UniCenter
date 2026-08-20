package it.project.controller;

import java.util.ArrayList;
import java.util.List;

import it.project.Materia;
import it.project.Notifica;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.database.ClockProvider;

/**
 * Controller (GRASP / Facade Controller) per il caso d'uso UC7 - Inviare Comunicazioni di Corso.
 * 
 * Flusso e Regole di Dominio:
 * 1. Il Professore seleziona una materia di cui è responsabile e compone un annuncio o avviso.
 * 2. Il sistema sincronizza il registro degli studenti iscritti alla materia:
 *    - La materia deve essere presente nel piano di studi dello studente.
 *    - La materia non deve essere già stata registrata (superata) nel libretto dello studente.
 * 3. L'oggetto Materia (Subject / Observable) distribuisce automaticamente l'annuncio
 *    a tutti gli studenti registrati (Observer).
 */
public class InvioComunicazioniController {

    private final Unicenter unicenter;
    private final GestoreMaterieController gestoreMaterie;

    public InvioComunicazioniController(Unicenter unicenter, GestoreMaterieController gestoreMaterie) {
        this.unicenter = unicenter;
        this.gestoreMaterie = gestoreMaterie;
    }

    /**
     * Invia un annuncio/comunicazione per una materia specifica.
     *
     * @param professore    il docente responsabile che pubblica l'annuncio
     * @param codiceMateria il codice della materia
     * @param titolo        l'oggetto o titolo della comunicazione
     * @param messaggio     il corpo del messaggio / testo dell'annuncio
     * @return il numero di studenti a cui è stata notificata la comunicazione
     * @throws IllegalArgumentException se i parametri non sono validi o il professore non è abilitato
     */
    public int inviaComunicazione(Professore professore, String codiceMateria, String titolo, String messaggio) {
        // 1. Validazione parametri
        if (professore == null) {
            throw new IllegalArgumentException("Professore non specificato o sessione non valida.");
        }
        if (codiceMateria == null || codiceMateria.trim().isEmpty()) {
            throw new IllegalArgumentException("Il codice materia è obbligatorio.");
        }
        if (titolo == null || titolo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della comunicazione è obbligatorio.");
        }
        if (messaggio == null || messaggio.trim().isEmpty()) {
            throw new IllegalArgumentException("Il testo della comunicazione è obbligatorio.");
        }

        // 2. Controllo abilitazione professore alla materia
        if (!gestoreMaterie.isProfessoreAbilitatoAMateria(professore.getIdProfessore(), codiceMateria.trim())) {
            throw new IllegalArgumentException(
                    "Il professore " + professore.getNome() + " " + professore.getCognome()
                            + " non è abilitato a inviare comunicazioni per la materia " + codiceMateria + ".");
        }

        // 3. Recupero Materia (Subject)
        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria.trim());
        if (materia == null) {
            throw new IllegalArgumentException("Materia non trovata: " + codiceMateria);
        }

        // 4. Sincronizzazione registro iscritti alla Materia (Regole di Dominio UC7)
        sincronizzaStudentiMateria(materia);

        // 5. Creazione della Notifica
        String oggetto = "[Avviso Corso - " + materia.getNome() + "] " + titolo.trim();
        String contenuto = messaggio.trim() + "\n(Docente: Prof. " + professore.getNome() + " " + professore.getCognome() + ")";
        Notifica notifica = new Notifica(oggetto, contenuto, ClockProvider.nowLocalDateTime());

        // 6. Dispacciamento tramite l'oggetto Materia (Observer Pattern)
        materia.notificaIscritti(notifica);

        // Notifica di conferma al docente
        Notifica notificaProf = new Notifica(
                "[Conferma Invio] " + oggetto,
                "Comunicazione inviata con successo a " + materia.getNumeroIscritti() + " studenti iscritti.",
                ClockProvider.nowLocalDateTime()
        );
        professore.aggiungiNotifica(notificaProf);

        return materia.getNumeroIscritti();
    }

    /**
     * Sincronizza il registro degli studenti iscritti alla materia applicando le Regole di Dominio:
     * - La materia deve essere nel piano di studi dello studente.
     * - La materia non deve essere già superata / registrata nel libretto.
     */
    public void sincronizzaStudentiMateria(Materia materia) {
        if (materia == null) return;
        String cod = materia.getCodiceMateria();

        List<Studente> tuttiStudenti = unicenter.getStudentiIscritti();
        for (Studente st : tuttiStudenti) {
            boolean inPianoStudi = st.getPianoDiStudi() != null && st.getPianoDiStudi().contieneMateria(cod);
            boolean esameNonSuperato = st.getLibretto() == null || !st.getLibretto().isEsameSuperato(cod);

            if (inPianoStudi && esameNonSuperato) {
                materia.iscriviStudente(st);
            } else {
                materia.disiscriviStudente(st);
            }
        }
    }

    /**
     * Restituisce la lista degli studenti attualmente destinatari per una data materia.
     */
    public List<Studente> getStudentiDestinatari(String codiceMateria) {
        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria);
        if (materia == null) return new ArrayList<>();

        sincronizzaStudentiMateria(materia);
        List<Studente> destinatari = new ArrayList<>();
        for (Object obs : materia.getIscritti()) {
            if (obs instanceof Studente) {
                destinatari.add((Studente) obs);
            }
        }
        return destinatari;
    }
}
