package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.util.Set;

/**
 * Miner for the consumption-controlled-by pattern.
 * @author Ozgun Babur
 */
public class ConsumptionControlledByMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ConsumptionControlledByMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CONSUMPTION_CONTROLLED_BY, ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.meabolicCatalysisSubclass(ubiqueIDs, true);
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
