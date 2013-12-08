package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.util.Set;

/**
 * Miner for the "used-for-production-of" relation.
 * @author Ozgun Babur
 */
public class UsedToProduceMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public UsedToProduceMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.USED_TO_PRODUCE, ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.usedForProductionOf(ubiqueIDs);
	}

	@Override
	public String getSourceLabel()
	{
		return "SMR1";
	}

	@Override
	public String getTargetLabel()
	{
		return "SMR2";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Conv"};
	}
}
