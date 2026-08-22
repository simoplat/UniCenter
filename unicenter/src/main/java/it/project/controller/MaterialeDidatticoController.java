package it.project.controller;

import java.io.IOException;
import java.util.*;

import it.project.Materia;
import it.project.Professore;
import it.project.Studente;
import it.project.Unicenter;
import it.project.materiale.*;
import it.project.materiale.repository.FileSystemMaterialeDidatticoRepository;
import it.project.materiale.repository.MaterialeDidatticoRepository;

/**
 * Controller (GRASP / Facade Controller GoF) per:
 * - UC6 – Gestire Materiale Didattico (Professore)
 * - UC10 – Consultare Materiale Didattico (Studente)
 *
 * Riceve le richieste dall'interfaccia (Web UI / Console), coordina le entità di dominio
 * (Composite ElementoDidattico, Cartella, MaterialeDidattico) e disaccoppia la logica
 * tramite Pure Fabrication / Indirection (MaterialeDidatticoRepository).
 */
public class MaterialeDidatticoController {

    private final Unicenter unicenter;
    private final GestoreMaterieController gestoreMaterie;
    private final MaterialeDidatticoRepository repository;

    public MaterialeDidatticoController(Unicenter unicenter, GestoreMaterieController gestoreMaterie) {
        this(unicenter, gestoreMaterie, new FileSystemMaterialeDidatticoRepository());
    }

    public MaterialeDidatticoController(Unicenter unicenter, GestoreMaterieController gestoreMaterie, MaterialeDidatticoRepository repository) {
        this.unicenter = unicenter;
        this.gestoreMaterie = gestoreMaterie;
        this.repository = repository;
    }

    public MaterialeDidatticoRepository getRepository() {
        return repository;
    }

    // =========================================================================
    // INIZIALIZZAZIONE STRUTTURA CARTELLE (Creator & Indirection)
    // =========================================================================

    /**
     * Inizializza la gerarchia fisica e logica della materia e delle cartelle dei professori associati.
     */
    public Cartella inizializzaMateria(Materia materia) {
        if (materia == null) return null;
        Cartella radice = materia.getCartellaRadice();
        repository.creaDirectory(radice.getPathRelativo());

        // Assicura la presenza delle cartelle per ciascun docente assegnato
        List<String> idDocenti = gestoreMaterie.trovaProfessoriDellaMateria(materia.getCodiceMateria());
        for (String idProf : idDocenti) {
            Optional<Professore> profOpt = unicenter.trovaProfessore(idProf);
            String nomeProf = profOpt.map(p -> p.getNome() + "_" + p.getCognome()).orElse(idProf);
            Cartella cartellaProf = materia.getOrCreateCartellaProfessore(idProf, nomeProf);
            repository.creaDirectory(cartellaProf.getPathRelativo());
        }
        return radice;
    }

    // =========================================================================
    // UC6 – GESTIRE MATERIALE DIDATTICO (PROFESSORE)
    // =========================================================================

    /**
     * Crea una nuova cartella all'interno della cartella del professore o di una sua sottocartella.
     */
    public Cartella creaCartella(Professore professore, String codiceMateria, String idCartellaGenitore,
                                 String nomeCartella, String descrizione) {
        validaProfessoreAbilitato(professore, codiceMateria);

        if (nomeCartella == null || nomeCartella.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della cartella non può essere vuoto.");
        }

        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria);
        if (materia == null) {
            throw new IllegalArgumentException("Materia non trovata: " + codiceMateria);
        }

        Cartella radice = materia.getCartellaRadice();
        Cartella cartellaGenitore = null;

        if (idCartellaGenitore == null || idCartellaGenitore.trim().isEmpty() || idCartellaGenitore.equals(radice.getId())) {
            // Se non specificata o radice materia, usa la cartella principale del professore
            String nomeProf = professore.getNome() + "_" + professore.getCognome();
            cartellaGenitore = materia.getOrCreateCartellaProfessore(professore.getIdProfessore(), nomeProf);
        } else {
            cartellaGenitore = radice.trovaCartella(idCartellaGenitore);
        }

        if (cartellaGenitore == null) {
            throw new IllegalArgumentException("Cartella di destinazione non trovata.");
        }

        // Verifica che la cartella genitore appartenga alla gerarchia del professore
        validaAccessoScritturaProfessore(professore, cartellaGenitore, materia);

        // Sanificazione nome
        String nomePulito = nomeCartella.trim().replaceAll("[^a-zA-Z0-9_\\-\\s]", "_");

        // Creator Pattern: Delega alla Cartella la creazione del nodo figlio
        Cartella nuovaCartella = cartellaGenitore.creaSubCartella(nomePulito, descrizione, professore.getIdProfessore());

        // Pure Fabrication: Crea directory fisica su FileSystem
        repository.creaDirectory(nuovaCartella.getPathRelativo());

        return nuovaCartella;
    }

    /**
     * Carica un nuovo materiale didattico (file, slide, dispensa, link, video) all'interno di una cartella del professore.
     */
    public MaterialeDidattico caricaMateriale(Professore professore, String codiceMateria, String idCartellaGenitore,
                                              String nomeFile, String descrizione, TipoMateriale tipo,
                                              byte[] contenuto) {
        validaProfessoreAbilitato(professore, codiceMateria);

        if (nomeFile == null || nomeFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del materiale non può essere vuoto.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Il tipo di materiale è obbligatorio.");
        }

        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria);
        if (materia == null) {
            throw new IllegalArgumentException("Materia non trovata: " + codiceMateria);
        }

        Cartella radice = materia.getCartellaRadice();
        Cartella cartellaGenitore = null;

        if (idCartellaGenitore == null || idCartellaGenitore.trim().isEmpty() || idCartellaGenitore.equals(radice.getId())) {
            String nomeProf = professore.getNome() + "_" + professore.getCognome();
            cartellaGenitore = materia.getOrCreateCartellaProfessore(professore.getIdProfessore(), nomeProf);
        } else {
            cartellaGenitore = radice.trovaCartella(idCartellaGenitore);
        }

        if (cartellaGenitore == null) {
            throw new IllegalArgumentException("Cartella di destinazione non trovata.");
        }

        validaAccessoScritturaProfessore(professore, cartellaGenitore, materia);

        String nomePulito = nomeFile.trim();

        // Creator Pattern: delega alla Cartella
        MaterialeDidattico materiale = cartellaGenitore.creaMateriale(
                nomePulito,
                descrizione,
                tipo,
                contenuto,
                professore.getIdProfessore(),
                repository
        );

        // Se non è un link o video esterno ma un file reale, lo persistiamo su disco
        if (tipo != TipoMateriale.LINK && (tipo != TipoMateriale.VIDEO || !materiale.getPathRelativo().startsWith("http"))) {
            try {
                repository.salvaFile(materiale.getPathRelativo(), contenuto);
            } catch (IOException e) {
                cartellaGenitore.rimuoviElemento(materiale.getId());
                throw new RuntimeException("Errore durante il salvataggio fisico del file: " + e.getMessage(), e);
            }
        }

        return materiale;
    }

    /**
     * Elimina una risorsa didattica o una cartella.
     */
    public boolean eliminaElemento(Professore professore, String codiceMateria, String idElemento) {
        validaProfessoreAbilitato(professore, codiceMateria);

        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria);
        if (materia == null) {
            throw new IllegalArgumentException("Materia non trovata: " + codiceMateria);
        }

        Cartella radice = materia.getCartellaRadice();
        ElementoDidattico elemento = radice.trovaElemento(idElemento);
        if (elemento == null) {
            throw new IllegalArgumentException("Elemento non trovato nel materiale didattico.");
        }

        // Non è consentito eliminare la cartella radice della materia
        if (elemento.getId().equals(radice.getId())) {
            throw new IllegalStateException("Impossibile eliminare la cartella radice della materia.");
        }

        // Verifica che l'elemento appartenga al professore
        validaAccessoScritturaProfessore(professore, elemento, materia);

        // Eliminazione fisica da storage
        if (elemento.isCartella()) {
            repository.eliminaDirectory(elemento.getPathRelativo());
        } else {
            repository.eliminaFile(elemento.getPathRelativo());
        }

        // Eliminazione logica dal Composite
        return radice.rimuoviElemento(idElemento);
    }

    // =========================================================================
    // UC10 – CONSULTARE MATERIALE DIDATTICO (STUDENTE & GENERALE)
    // =========================================================================

    /**
     * Restituisce l'albero completo dei materiali per una data materia.
     */
    public Cartella getAlberoMateria(String codiceMateria) {
        Materia materia = gestoreMaterie.trovaMaterieByCodice(codiceMateria);
        if (materia == null) {
            throw new IllegalArgumentException("Materia non trovata: " + codiceMateria);
        }
        inizializzaMateria(materia);
        return materia.getCartellaRadice();
    }

    /**
     * Cerca un elemento in tutte le materie del sistema dato il suo ID.
     */
    public ElementoDidattico trovaElementoById(String idElemento) {
        if (idElemento == null) return null;
        for (Materia m : gestoreMaterie.getTutteLeMaterie()) {
            inizializzaMateria(m);
            ElementoDidattico trovato = m.getCartellaRadice().trovaElemento(idElemento);
            if (trovato != null) {
                return trovato;
            }
        }
        return null;
    }

    /**
     * Polymorphism: Consulta l'anteprima polimorfica di una risorsa.
     */
    public AnteprimaRisultato consultaMateriale(String idElemento) {
        ElementoDidattico elemento = trovaElementoById(idElemento);
        if (elemento == null) {
            throw new IllegalArgumentException("Materiale didattico non trovato con ID: " + idElemento);
        }
        return elemento.visualizza();
    }

    /**
     * Polymorphism: Scarica i byte della risorsa.
     */
    public DownloadResponse scaricaMateriale(String idElemento) {
        ElementoDidattico elemento = trovaElementoById(idElemento);
        if (elemento == null) {
            throw new IllegalArgumentException("Materiale didattico non trovato con ID: " + idElemento);
        }

        if (elemento.isCartella()) {
            throw new IllegalArgumentException("Non è possibile scaricare direttamente una cartella come singolo file.");
        }

        MaterialeDidattico materiale = (MaterialeDidattico) elemento;
        byte[] bytes = materiale.scarica();
        String mime = materiale.getMimeType();

        return new DownloadResponse(materiale.getNome(), mime, bytes);
    }

    // =========================================================================
    // UC10 – GESTIONE PREFERITI STUDENTE
    // =========================================================================

    /**
     * Attiva/Disattiva lo stato di preferito di un elemento per lo studente.
     */
    public boolean togglePreferito(Studente studente, String idElemento) {
        if (studente == null) {
            throw new IllegalArgumentException("Studente non valido.");
        }
        ElementoDidattico elemento = trovaElementoById(idElemento);
        if (elemento == null) {
            throw new IllegalArgumentException("Elemento non trovato con ID: " + idElemento);
        }
        return studente.togglePreferito(idElemento);
    }

    /**
     * Restituisce la lista di tutti gli elementi preferiti salvati dallo studente.
     */
    public List<ElementoDidattico> getPreferitiStudente(Studente studente) {
        if (studente == null) return Collections.emptyList();
        List<String> ids = studente.getPreferitiMaterialeIds();
        List<ElementoDidattico> preferiti = new ArrayList<>();

        for (String id : ids) {
            ElementoDidattico e = trovaElementoById(id);
            if (e != null) {
                preferiti.add(e);
            }
        }
        return preferiti;
    }

    // =========================================================================
    // VALIDAZIONI E CONTROLLI DI SICUREZZA
    // =========================================================================

    private void validaProfessoreAbilitato(Professore professore, String codiceMateria) {
        if (professore == null) {
            throw new IllegalStateException("Nessun professore autenticato.");
        }
        if (!gestoreMaterie.isProfessoreAbilitatoAMateria(professore.getIdProfessore(), codiceMateria)) {
            throw new SecurityException("Il Professore non è abilitato all'insegnamento della materia " + codiceMateria + ".");
        }
    }

    private void validaAccessoScritturaProfessore(Professore professore, ElementoDidattico elemento, Materia materia) {
        String ownerId = elemento.getOwnerProfessoreId();
        if (ownerId != null && !ownerId.equals(professore.getIdProfessore())) {
            throw new SecurityException("Non sei autorizzato a modificare o inserire file nelle cartelle di un altro docente.");
        }
    }

    // =========================================================================
    // DTO PER DOWNLOAD
    // =========================================================================

    public static class DownloadResponse {
        private final String nomeFile;
        private final String mimeType;
        private final byte[] bytes;

        public DownloadResponse(String nomeFile, String mimeType, byte[] bytes) {
            this.nomeFile = nomeFile;
            this.mimeType = mimeType;
            this.bytes = bytes != null ? bytes : new byte[0];
        }

        public String getNomeFile() { return nomeFile; }
        public String getMimeType() { return mimeType; }
        public byte[] getBytes() { return bytes; }
    }
}
