package org.biopax.paxtools.io.pathwayCommons.util;

public class ErrorUtil {
    public static PathwayCommonsException createException(org.biopax.paxtools.io.pathwayCommons.model.Error error) {
        switch(error.getErrorCode()) {
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
