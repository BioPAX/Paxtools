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
	 * Constructor for extension purposes.
	 */
	protected ConsumptionControlledByMiner(SIFType type)
	{
		super(type);
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
		return "controller ER";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"controller simple PE", "controller PE"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"part PE", "part SM"};
	}
}
