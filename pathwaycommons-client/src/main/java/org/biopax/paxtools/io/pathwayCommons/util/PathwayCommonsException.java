package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorResponse;

import java.io.IOException;

/**
 * See http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class PathwayCommonsException extends IOException {
    private ErrorResponse error;

    public PathwayCommonsException(ErrorResponse error) {
        super(error.toString());
        this.error = error;
    }

    public ErrorResponse getError() {
        return error;
    }
}
