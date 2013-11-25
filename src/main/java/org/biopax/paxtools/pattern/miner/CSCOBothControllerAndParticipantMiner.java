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
 * Miner for the controls-state-change pattern. This time the controller is also an input.
 * @author Ozgun Babur
 */
public class CSCOBothControllerAndParticipantMiner extends MinerAdapter
	implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public CSCOBothControllerAndParticipantMiner()
	{
		super("cso-both-ctrl-part",
			"Finds relations between proteins where " +
			"the first one is controlling a reaction that changes the state of the second one. " +
			"The controller is also an input. The reaction has to be a Conversion and modified " +
			"Protein should be represented with different non-generic PhysicalEntity on each " +
			"side.", null);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChangeBothControlAndPart();
	}

	/**
	 * Writes the result as "A B", where A and B are gene symbols, and whitespace is tab.
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
		return "changed ER";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONTROLS_STATE_CHANGE_OF;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
