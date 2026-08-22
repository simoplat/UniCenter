package it.project.materiale.repository;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Pure Fabrication & Indirection & Protected Variations (GRASP):
 * Interfaccia di intermediazione tra il modello di dominio del materiale didattico
 * e il meccanismo di persistenza fisica (FileSystem o futuro Cloud Storage).
 */
public interface MaterialeDidatticoRepository {

    /**
     * Salva il contenuto binario di un file sul path relativo specificato.
     */
    void salvaFile(String pathRelativo, byte[] contenuto) throws IOException;

    /**
     * Legge e restituisce il contenuto binario del file al path relativo.
     */
    byte[] leggiFile(String pathRelativo) throws IOException;

    /**
     * Elimina un file al path relativo specificato.
     */
    boolean eliminaFile(String pathRelativo);

    /**
     * Crea ricorsivamente la directory al path relativo specificato se non esiste.
     */
    void creaDirectory(String pathRelativo);

    /**
     * Elimina una directory e il suo contenuto al path relativo specificato.
     */
    boolean eliminaDirectory(String pathRelativo);

    /**
     * Verifica se un file o directory esiste al path relativo specificato.
     */
    boolean esiste(String pathRelativo);

    /**
     * Restituisce la dimensione in byte di un file su disco.
     */
    long getDimensioneFile(String pathRelativo);

    /**
     * Restituisce il Path assoluto di radice dello storage.
     */
    Path getRootPath();
}
