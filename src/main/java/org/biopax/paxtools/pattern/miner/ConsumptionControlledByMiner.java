package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the consumption-controlled-by pattern.
 * @author Ozgun Babur
 */
public class ConsumptionControlledByMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ConsumptionControlledByMiner()
	{
		super(SIFEnum.CONSUMPTION_CONTROLLED_BY);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsMetabolicCatalysis(blacklist, true);
	}

	@Override
	public String getSourceLabel()
	{
		return "part SMR";
	}

	@Override
	public String getTargetLabel()
	{
		return "controller PR";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
