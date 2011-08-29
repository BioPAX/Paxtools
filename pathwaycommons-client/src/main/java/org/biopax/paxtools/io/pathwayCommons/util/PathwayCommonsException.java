package org.biopax.paxtools.io.pathwayCommons.util;

import org.biopax.paxtools.io.pathwayCommons.model.Error;

import java.io.IOException;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class PathwayCommonsException extends IOException {
    private Error error;

    public PathwayCommonsException(Error error) {
        super(error.toString());
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
