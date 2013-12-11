package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.util.Blacklist;

import java.util.Set;

/**
 * Miner for the "reacts-with" relation.
 * @author Ozgun Babur
 */
public class ReactsWithMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ReactsWithMiner(Blacklist blacklist)
	{
		super(SIFType.REACTS_WITH, blacklist);
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
