package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorResponse;

public class BadRequestException extends PathwayCommonsException {
    public BadRequestException(ErrorResponse error) {
        super(error);
    }
}
