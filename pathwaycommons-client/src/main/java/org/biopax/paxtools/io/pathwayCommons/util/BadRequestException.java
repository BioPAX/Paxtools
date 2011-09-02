package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorType;

public class BadRequestException extends PathwayCommonsException {
    public BadRequestException(ErrorType error) {
        super(error);
    }
}
