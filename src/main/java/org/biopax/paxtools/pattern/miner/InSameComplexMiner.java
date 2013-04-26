package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for the degradation pattern.
 * @author Ozgun Babur
 */
public class InSameComplexMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public InSameComplexMiner()
	{
		super("Members-of-same-complex", "This miner finds cases where two genes have states " +
			"that are members of the same complex.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.appearInSameComplex();
	}

	/**
	 * Writes the result as "A DEGRADES B" or "A BLOCKS_DEGRADATION B", where A and B are gene
	 * symbols, and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultAsSIF(matches, out, false, getSourceLabel(), getTargetLabel());
	}

	@Override
	public String getSourceLabel()
	{
		return "Protein 1";
	}

	@Override
	public String getTargetLabel()
	{
		return "Protein 2";
	}

	/**
	 * The relation can be either degrades or blocks-degradation.
	 * @param m the match
	 * @return type
	 */
	@Override
	public String getRelationType(Match m)
	{
		return "in-same-complex";
	}

	@Override
	public boolean isDirected()
	{
		return false;
	}
}
