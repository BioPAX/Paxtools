package org.biopax.paxtools.io.pathwayCommons.util;

import org.biopax.paxtools.io.pathwayCommons.model.Error;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class BadCommandException extends PathwayCommonsException {
    public BadCommandException(Error error) {
        super(error);
    }
}
