package org.biopax.paxtools.io.pathwayCommons.model;

/**
 * Class mapping of http://www.pathwaycommons.org/pc2-demo/#errors
 */
public class Error {
    private Integer error_code;
    private String error_msg;
    private String error_details;

    public Integer getErrorCode() {
        return error_code;
    }

    public String getErrorMessage() {
        return error_msg;
    }

    public String getErrorDetails() {
        return error_details;
    }

    public String toString() {
        return error_code + ": " + error_msg + " (" + error_details + ").";
    }
}
