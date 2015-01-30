package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-production-of pattern.
 * @author Ozgun Babur
 */
public class ControlsProductionOfMiner extends ConsumptionControlledByMiner
{
	/**
	 * Constructor that sets name, description and type.
	 */
	public ControlsProductionOfMiner()
	{
		super(SIFEnum.CONTROLS_PRODUCTION_OF);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsMetabolicCatalysis(blacklist, false);
	}

	@Override
	public String getSourceLabel()
	{
		return "controller ER";
	}

	@Override
	public String getTargetLabel()
	{
		return "part SMR";
	}
}
