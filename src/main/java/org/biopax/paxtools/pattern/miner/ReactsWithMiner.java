package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

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
	public ReactsWithMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.REACTS_WITH, ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.reactsWith(ubiqueIDs);
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
