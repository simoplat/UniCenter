package it.project.validation;

import it.project.Appello;
import it.project.CorsoDiLaurea;
import it.project.PianoDiStudi;
import it.project.Studente;
import it.project.Unicenter;
import it.project.controller.GestioneCorsiLaureaController;
import it.project.exceptions.validator.AnnoCorsoNonValidoException;
import it.project.exceptions.validator.IscrizioneNonValidaException;

/**
 * Validatore Chain of Responsibility:
 * Verifica che per le materie obbligatorie, la materia richiesta dall'appello
 * appartenga allo stesso anno di corso dello studente o ad un anno precedente (es. materie arretrate).
 * Questo controllo NON si applica alle materie a scelta.
 */
public class AnnoCorsoMateriaValidator extends AbstractIscrizioneValidator {

    private final GestioneCorsiLaureaController gestioneCorsiController;

    /**
     * Costruttore di default. Risolve il controller dei corsi via Singleton Unicenter.
     */
    public AnnoCorsoMateriaValidator() {
        this.gestioneCorsiController = null;
    }

    /**
     * Costruttore con iniezione del controller dei corsi di laurea.
     *
     * @param gestioneCorsiController controller gestione corsi
     */
    public AnnoCorsoMateriaValidator(GestioneCorsiLaureaController gestioneCorsiController) {
        this.gestioneCorsiController = gestioneCorsiController;
    }

    @Override
    public boolean validate(Studente studente, Appello appello) throws IscrizioneNonValidaException {
        if (studente == null || appello == null) {
            return checkNext(studente, appello);
        }

        String codiceMateria = appello.getCodiceMateria();
        PianoDiStudi piano = studente.getPianoDiStudi();

        // Il controllo non si applica per le materie a scelta
        if (piano != null && piano.isMateriaAScelta(codiceMateria)) {
            return checkNext(studente, appello);
        }

        // Recupero del Corso di Laurea dello studente
        CorsoDiLaurea corso = null;
        try {
            if (gestioneCorsiController != null) {
                corso = gestioneCorsiController.trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());
            } else {
                Unicenter unicenter = Unicenter.getInstance();
                if (unicenter != null && unicenter.getGestioneCorsiLaureaController() != null) {
                    corso = unicenter.getGestioneCorsiLaureaController().trovaCorsoDiLaureaById(studente.getIdCorsoDiLaurea());
                }
            }
        } catch (it.project.exceptions.CorsoDiLaureaNonTrovatoException e) {
            corso = null;
        }

        if (corso != null) {
            int annoMateria = corso.getAnnoDellaMateria(codiceMateria);
            int annoCorrenteStudente = studente.getAnnoCorrente();

            // La materia obbligatoria deve appartenere all'anno corrente o a un anno precedente
            if (annoMateria > 0 && annoMateria > annoCorrenteStudente) {
                throw new AnnoCorsoNonValidoException(
                        "Iscrizione rifiutata: la materia obbligatoria '" + codiceMateria + "' è prevista per il "
                                + annoMateria + "° anno, mentre lo studente è attualmente iscritto al "
                                + annoCorrenteStudente + "° anno.");
            }
        }

        return checkNext(studente, appello);
    }
}
