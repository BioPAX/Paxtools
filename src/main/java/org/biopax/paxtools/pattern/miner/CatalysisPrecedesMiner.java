package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the catalysis-precedes pattern.
 * @author Ozgun Babur
 */
public class CatalysisPrecedesMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public CatalysisPrecedesMiner()
	{
		super(SIFEnum.CATALYSIS_PRECEDES);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.catalysisPrecedes(blacklist);
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

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"first simple controller PE", "first controller PE"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"second simple controller PE", "second controller PE"};
	}
}
