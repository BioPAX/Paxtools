package org.biopax.paxtools.io.pathwayCommons.util;

import org.biopax.paxtools.io.pathwayCommons.model.Error;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class NoResultsFoundException extends PathwayCommonsException {
    public NoResultsFoundException(Error error) {
        super(error);
    }
}
