package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the "used-for-production-of" relation.
 * @author Ozgun Babur
 */
public class UsedToProduceMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets sif type.
	 */
	public UsedToProduceMiner()
	{
		super(SIFEnum.USED_TO_PRODUCE);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.usedToProduce(blacklist);
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
