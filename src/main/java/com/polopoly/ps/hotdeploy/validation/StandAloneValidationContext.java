package com.polopoly.ps.hotdeploy.validation;

import java.util.HashSet;
import java.util.Set;

import com.polopoly.ps.hotdeploy.text.TextContent;

public class StandAloneValidationContext implements ValidationContext {
    private Set<String> added = new HashSet<String>();

    public StandAloneValidationContext() {
    }

    public void validateContentExistence(String externalId) throws ValidationException {
        if (!added.contains(externalId)) {
            throw new ValidationException("The external ID \"" + externalId + "\" was unknown.");
        }
    }

    public void validateTemplateExistence(String externalId) throws ValidationException {
    }

    public void add(TextContent textContent) {
        added.add(textContent.getId());
    }

    public void validateTextContentExistence(String textContentId) throws ValidationException {
        if (!added.contains(textContentId)) {
            throw new ValidationException("The external ID \"" + textContentId +
                    "\" must be imported as part of the same import batch.");
        }
    }

}
