package org.biopax.paxtools.io.pathwayCommons.util;

import cpath.service.jaxb.ErrorResponse;

public class ErrorUtil {
    public static PathwayCommonsException createException(ErrorResponse error) {
        switch(error.getErrorCode().intValue()) {
            case 460:
                return new NoResultsFoundException(error);
            case 450:
                return new BadCommandException(error);
            case 452:
                return new BadRequestException(error);
            case 500:
                return new InternalServerErrorException(error);
            default:
                return new PathwayCommonsException(error);
        }
    }
}
