package org.biopax.paxtools.util;

/**
 */
public class IllegalBioPAXArgumentException extends IllegalArgumentException
{
// --------------------------- CONSTRUCTORS ---------------------------

	public IllegalBioPAXArgumentException()
	{
	}

	public IllegalBioPAXArgumentException(Throwable cause)
	{
		super(cause);
	}

	public IllegalBioPAXArgumentException(String s)
	{
		super(s);
	}

	public IllegalBioPAXArgumentException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
