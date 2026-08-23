package it.project.materiale.repository;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Pure Fabrication, Indirection e Protected Variations (GRASP):
 * Interfaccia di intermediazione tra il modello di dominio del materiale didattico
 * e il meccanismo di persistenza fisica (FileSystem o storage esterno).
 */
public interface MaterialeDidatticoRepository {

    /**
     * Salva il contenuto binario di un file sul path relativo specificato.
     *
     * @param pathRelativo percorso relativo di destinazione
     * @param contenuto    array di byte del file
     * @throws IOException in caso di errori di I/O
     */
    void salvaFile(String pathRelativo, byte[] contenuto) throws IOException;

    /**
     * Legge e restituisce il contenuto binario del file al path relativo.
     *
     * @param pathRelativo percorso relativo del file
     * @return array di byte del file
     * @throws IOException in caso di errori di lettura
     */
    byte[] leggiFile(String pathRelativo) throws IOException;

    /**
     * Elimina un file al path relativo specificato.
     *
     * @param pathRelativo percorso relativo del file da eliminare
     * @return true se eliminato con successo
     */
    boolean eliminaFile(String pathRelativo);

    /**
     * Crea ricorsivamente la directory al path relativo specificato se non esiste.
     *
     * @param pathRelativo percorso relativo della directory
     */
    void creaDirectory(String pathRelativo);

    /**
     * Elimina una directory e il suo contenuto al path relativo specificato.
     *
     * @param pathRelativo percorso relativo della directory
     * @return true se eliminata con successo
     */
    boolean eliminaDirectory(String pathRelativo);

    /**
     * Verifica se un file o directory esiste al path relativo specificato.
     *
     * @param pathRelativo percorso relativo da verificare
     * @return true se presente
     */
    boolean esiste(String pathRelativo);

    /**
     * Restituisce la dimensione in byte di un file su disco.
     *
     * @param pathRelativo percorso relativo del file
     * @return dimensione in byte o 0 se non esiste
     */
    long getDimensioneFile(String pathRelativo);

    /**
     * Restituisce il Path assoluto di radice dello storage.
     *
     * @return Path assoluto radice
     */
    Path getRootPath();
}
