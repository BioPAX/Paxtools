package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the degradation pattern.
 * @author Ozgun Babur
 */
public class CSCOThroughDegradationMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public CSCOThroughDegradationMiner()
	{
		super(SIFEnum.CONTROLS_STATE_CHANGE_OF);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChangeThroughDegradation();
	}

	@Override
	public String getSourceLabel()
	{
		return "upstream ER";
	}

	@Override
	public String getTargetLabel()
	{
		return "downstream ER";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"upstream SPE", "upstream PE"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"input PE", "input SPE"};
	}
}
