package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorResponse;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class BadCommandException extends PathwayCommonsException {
    public BadCommandException(ErrorResponse error) {
        super(error);
    }
}
