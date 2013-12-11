package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.util.Blacklist;

import java.util.Set;

/**
 * Miner for the controls-transport-of-chemical pattern.
 * @author Ozgun Babur
 */
public class ControlsTransportOfChemicalMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsTransportOfChemicalMiner(Blacklist blacklist)
	{
		super(SIFType.CONTROLS_TRANSPORT_OF_CHEMICAL, blacklist);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsTransportOfChemical(blacklist);
	}

	@Override
	public String getSourceLabel()
	{
		return "controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed SMR";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
