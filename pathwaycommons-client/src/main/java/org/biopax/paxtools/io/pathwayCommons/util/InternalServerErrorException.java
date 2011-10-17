package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorResponse;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class InternalServerErrorException extends PathwayCommonsException {
    public InternalServerErrorException(ErrorResponse error) {
        super(error);
    }
}
