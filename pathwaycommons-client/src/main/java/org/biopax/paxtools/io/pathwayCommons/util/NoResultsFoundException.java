package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorResponse;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class NoResultsFoundException extends PathwayCommonsException {
    public NoResultsFoundException(ErrorResponse error) {
        super(error);
    }
}
