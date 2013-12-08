package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

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
	public ControlsTransportOfChemicalMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CONTROLS_TRANSPORT_OF_CHEMICAL, ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.transportsChemical(ubiqueIDs);
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
