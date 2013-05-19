package org.biopax.paxtools.io.sif;

/**
 * This exception is thrown when there are more than a maximum number of interactions generated.
 */
public class MaximumInteractionThresholdExceedException extends RuntimeException
{
	/**
	 * Constructor with the message.
	 * @param s message
	 */
    public MaximumInteractionThresholdExceedException(String s)
    {
        super(s);
    }
}
