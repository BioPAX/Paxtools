package org.biopax.paxtools.util;

/**
 */
public class BioPaxIOException extends RuntimeException
{
// --------------------------- CONSTRUCTORS ---------------------------

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
