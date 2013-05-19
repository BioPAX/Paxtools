package org.biopax.paxtools.util;

/**
 * This exception is thrown typically when BioPAX domain or cardinality restrictions are violated. It may also be
 * thrown when a class or property lookup by name fails.
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
