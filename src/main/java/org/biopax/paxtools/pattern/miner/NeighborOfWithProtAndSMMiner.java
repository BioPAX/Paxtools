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
 * Miner for the "neighbor-of" relation. This is non-standard version that includes small molecules.
 * @author Ozgun Babur
 */
public class NeighborOfWithProtAndSMMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public NeighborOfWithProtAndSMMiner()
	{
		super(SIFType.NEIGHBOR_OF.getTag() + "-with-prot-sm", "This miner finds cases where two " +
			"genes or small molecules are participants or controllers of the same interaction.",
			null);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.neighborOfWithProtAndSM();
	}

	/**
	 * Writes the result as "A neighbor-of B", where A and B are gene symbols or chemical names, and
	 * whitespace is tab.
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
		return "Entity 1";
	}

	@Override
	public String getTargetLabel()
	{
		return "Entity 2";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.NEIGHBOR_OF;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Inter"};
	}
}
