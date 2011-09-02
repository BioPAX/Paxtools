package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorType;

import java.io.IOException;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class PathwayCommonsException extends IOException {
    private ErrorType error;

    public PathwayCommonsException(ErrorType error) {
        super(error.toString());
        this.error = error;
    }

    public ErrorType getError() {
        return error;
    }
}
