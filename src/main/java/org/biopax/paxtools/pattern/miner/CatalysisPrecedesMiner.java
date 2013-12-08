package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.util.Set;

/**
 * Miner for the catalysis-precedes pattern.
 * @author Ozgun Babur
 */
public class CatalysisPrecedesMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public CatalysisPrecedesMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CATALYSIS_PRECEDES, ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.catalysisPrecedes(ubiqueIDs);
	}

	@Override
	public String getSourceLabel()
	{
		return "first PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "second PR";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"first Control", "first Conversion", "second Control",
			"second Conversion"};
	}
}
