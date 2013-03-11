package org.biopax.paxtools.util;

/**
 * This exception is thrown when an object does not have a proper, non-null, unique id.
 */
public class IllegalRDFIDException extends IllegalArgumentException
{
// --------------------------- CONSTRUCTORS ---------------------------

	public IllegalRDFIDException()
	{
	}

	public IllegalRDFIDException(Throwable cause)
	{
		super(cause);
	}

	public IllegalRDFIDException(String s)
	{
		super(s);
	}

	public IllegalRDFIDException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
