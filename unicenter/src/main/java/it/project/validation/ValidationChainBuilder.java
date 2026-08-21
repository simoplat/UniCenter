package it.project.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationChainBuilder {
    private final List<IscrizioneValidator> validators = new ArrayList<>();

    public ValidationChainBuilder addValidator(IscrizioneValidator validator) {
        validators.add(validator);
        return this;
    }

    public IscrizioneValidator build() {
        if (validators.isEmpty()) {
            throw new IllegalStateException("Nessun validatore presente nella catena di validazione.");
        }
        for (int i = 0; i < validators.size() - 1; i++) {
            validators.get(i).setNext(validators.get(i + 1));
        }
        return validators.get(0);
    }

    public static IscrizioneValidator buildDefaultChain() {
        IscrizioneValidator esameSuperato = new EsameSuperatoValidator();
        IscrizioneValidator pianoStudi = new PianoStudiValidator();
        IscrizioneValidator posti = new PostiDisponibiliValidator();
        IscrizioneValidator tasse = new TassaPaidValidator();
        IscrizioneValidator cognome = new CognomeFasciaValidator();
        IscrizioneValidator dataTermine = new DataTermineIscrizioneValidator();

        // Collega i validatori in sequenza
        esameSuperato.setNext(pianoStudi);
        pianoStudi.setNext(posti);
        posti.setNext(tasse);
        tasse.setNext(cognome);
        cognome.setNext(dataTermine);

        return esameSuperato;
    }
}