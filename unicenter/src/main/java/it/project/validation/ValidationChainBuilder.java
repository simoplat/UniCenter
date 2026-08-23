package it.project.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder per la composizione modulare della catena di validazione iscrizioni agli appelli
 * (Pattern Chain of Responsibility).
 */
public class ValidationChainBuilder {
    private final List<IscrizioneValidator> validators = new ArrayList<>();

    /**
     * Aggiunge un validatore alla catena.
     *
     * @param validator validatore da concatenare
     * @return this builder per concatenazione fluente
     */
    public ValidationChainBuilder addValidator(IscrizioneValidator validator) {
        validators.add(validator);
        return this;
    }

    /**
     * Costruisce la catena collegando ciascun validatore al successivo.
     *
     * @return primo elemento (testa) della catena
     * @throws IllegalStateException se nessun validatore è stato aggiunto
     */
    public IscrizioneValidator build() {
        if (validators.isEmpty()) {
            throw new IllegalStateException("Nessun validatore presente nella catena di validazione.");
        }
        for (int i = 0; i < validators.size() - 1; i++) {
            validators.get(i).setNext(validators.get(i + 1));
        }
        return validators.get(0);
    }

    /**
     * Costruisce la catena standard di default con tutti i 7 controlli di business.
     *
     * @return primo validatore della catena standard
     */
    public static IscrizioneValidator buildDefaultChain() {
        IscrizioneValidator esameSuperato = new EsameSuperatoValidator();
        IscrizioneValidator pianoStudi = new PianoStudiValidator();
        IscrizioneValidator annoCorso = new AnnoCorsoMateriaValidator();
        IscrizioneValidator posti = new PostiDisponibiliValidator();
        IscrizioneValidator tasse = new TassaPaidValidator();
        IscrizioneValidator cognome = new CognomeFasciaValidator();
        IscrizioneValidator dataTermine = new DataTermineIscrizioneValidator();

        // Collega i validatori in sequenza
        esameSuperato.setNext(pianoStudi);
        pianoStudi.setNext(annoCorso);
        annoCorso.setNext(posti);
        posti.setNext(tasse);
        tasse.setNext(cognome);
        cognome.setNext(dataTermine);

        return esameSuperato;
    }
}