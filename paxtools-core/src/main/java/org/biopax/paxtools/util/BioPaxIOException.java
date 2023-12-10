package org.biopax.paxtools.util;

/**
 * This exception is thrown if a problem occurs during an input/output operation related to BioPAX Handlers.
 */
public class BioPaxIOException extends RuntimeException
{
	public BioPaxIOException(Throwable cause)
	{
		super(cause);
	}

	public BioPaxIOException(String message)
	{
		super(message);
	}

    public BioPaxIOException(String message, Throwable e) {
        super(message, e);
    }
}
