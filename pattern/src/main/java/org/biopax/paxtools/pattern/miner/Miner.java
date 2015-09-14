package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * A miner provides a pattern to mine, and knows how to use the result set to prepare the text
 * output.
 *
 * @author Ozgun Babur
 */
public interface Miner
{
	/**
	 * Gets name of the miner.
	 * @return name
	 */
	public String getName();

	/**
	 * Gets description of the miner.
	 * @return description
	 */
	public String getDescription();

	/**
	 * Gets the pattern to use for mining the graph.
	 * @return the pattern
	 */
	public Pattern getPattern();

	/**
	 * Writes the text output to the given stream.
	 * @param matches pattern search result
	 * @param out output stream
	 * @throws IOException when there's a problem writing to the output stream
	 */
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out) throws IOException;
}
