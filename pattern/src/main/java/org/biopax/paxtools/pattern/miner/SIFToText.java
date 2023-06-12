package org.biopax.paxtools.pattern.miner;

/**
 * An interface for converting a SIF interaction to text.
 *
 * @author Ozgun Babur
 */
public interface SIFToText
{
	/**
	 * Creates the textual data corresponding to the given binary interaction.
	 * @param inter the interaction
	 * @return text data
	 */
	String convert(SIFInteraction inter);
}
