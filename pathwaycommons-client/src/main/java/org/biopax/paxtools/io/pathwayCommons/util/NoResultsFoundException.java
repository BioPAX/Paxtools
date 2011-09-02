package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorType;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class NoResultsFoundException extends PathwayCommonsException {
    public NoResultsFoundException(ErrorType error) {
        super(error);
    }
}
