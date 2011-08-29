package org.biopax.paxtools.io.pathwayCommons.util;

import org.biopax.paxtools.io.pathwayCommons.model.Error;

public class BadRequestException extends PathwayCommonsException {
    public BadRequestException(Error error) {
        super(error);
    }
}
