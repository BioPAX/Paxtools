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
 * Yet another miner for the controls-degradation pattern. This one searches two step relations.
 *
 * NOTE: THIS PATTERN DOES NOT WORK. KEEPING ONLY FOR HISTORICAL REASONS.
 *
 * @author Ozgun Babur
 */
public class ControlsDegradationIndirectMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsDegradationIndirectMiner()
	{
		super(SIFType.CONTROLS_DEGRADATION_OF.getTag() + "-indirectly", "This pattern finds relations " +
			"where first protein is controlling a Conversion whose output molecule is degraded " +
			"without a control.", null);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsDegradationIndirectly();
	}

	/**
	 * Writes the result as "A controls-degradation B", where A and B are gene symbols, and
	 * whitespace is tab.
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
		return "controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed PR";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONTROLS_DEGRADATION_OF;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion", "degrading Conv"};
	}
}
