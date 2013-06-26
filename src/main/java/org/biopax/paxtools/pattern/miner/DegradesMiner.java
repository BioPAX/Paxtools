package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for the degradation pattern.
 * @author Ozgun Babur
 */
public class DegradesMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public DegradesMiner()
	{
		super(SIFType.CONTROLS_DEGRADATION.getTag(), "This pattern finds relations where first " +
			"protein is controlling a Conversion that degrades the second protein.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.degradation();
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
		writeResultAsSIF(matches, out, true, getSourceLabel(), getTargetLabel());
	}

	/**
	 * Sets header of the output.
	 * @return header
	 */
	@Override
	public String getHeader()
	{
		return "Upstream\tRelation\tDownstream";
	}

	@Override
	public String getSourceLabel()
	{
		return "upstream PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "downstream PR";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONTROLS_DEGRADATION;
	}

	@Override
	public String[] getPubmedHarvestableLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
