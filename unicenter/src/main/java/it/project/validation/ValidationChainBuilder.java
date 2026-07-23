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
            return null;
        }
        for (int i = 0; i < validators.size() - 1; i++) {
            validators.get(i).setNext(validators.get(i + 1));
        }
        return validators.get(0);
    }
}