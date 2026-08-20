package it.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.project.observer.ObserverNotifica;

/**
 * Entità Materia.
 * Nel caso d'uso UC7 - Inviare Comunicazioni di Corso agisce da Subject / Observable (GoF Comportamentale),
 * mantenendo il registro degli studenti iscritti alla materia e notificandoli quando
 * il professore pubblica un nuovo avviso o comunicazione.
 */
public class Materia {
    private String codiceMateria;
    private String nome;
    private int cfu;
    private final List<ObserverNotifica> iscritti;

    public Materia(String codiceMateria, String nome, int cfu) {
        this.codiceMateria = codiceMateria;
        this.nome = nome;
        this.cfu = cfu;
        this.iscritti = new ArrayList<>();
    }

    public String getCodiceMateria() { return codiceMateria; }
    public String getNome() { return nome; }
    public int getCfu() { return cfu; }

    // =========================================================================
    // OBSERVER PATTERN: Subject / Observable per Comunicazioni di Corso (UC7)
    // =========================================================================

    /**
     * Iscrive uno studente come osservatore della materia.
     */
    public void iscriviStudente(ObserverNotifica studente) {
        if (studente != null && !iscritti.contains(studente)) {
            iscritti.add(studente);
        }
    }

    /**
     * Disiscrive uno studente dalla lista degli osservatori.
     */
    public void disiscriviStudente(ObserverNotifica studente) {
        if (studente != null) {
            iscritti.remove(studente);
        }
    }

    /**
     * Notifica automaticamente tutti gli studenti iscritti alla materia.
     */
    public void notificaIscritti(Notifica notifica) {
        for (ObserverNotifica observer : new ArrayList<>(iscritti)) {
            observer.riceviNotifica(notifica);
        }
    }

    /**
     * Restituisce la lista immutabile degli iscritti correnti.
     */
    public List<ObserverNotifica> getIscritti() {
        return Collections.unmodifiableList(iscritti);
    }

    public int getNumeroIscritti() {
        return iscritti.size();
    }

    @Override
    public String toString() {
        return "Materia [codiceMateria=" + codiceMateria + ", nome=" + nome + ", cfu=" + cfu + "]";
    }
}