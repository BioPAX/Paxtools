package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the degradation pattern.
 * @author Ozgun Babur
 */
public class ControlsDegradationMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsDegradationMiner()
	{
		super(SIFEnum.CONTROLS_DEGRADATION_OF);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsDegradation();
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
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
