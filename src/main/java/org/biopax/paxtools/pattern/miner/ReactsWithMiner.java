package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the "reacts-with" relation.
 * @author Ozgun Babur
 */
public class ReactsWithMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets sif type.
	 */
	public ReactsWithMiner()
	{
		super(SIFEnum.REACTS_WITH);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.reactsWith(blacklist);
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
