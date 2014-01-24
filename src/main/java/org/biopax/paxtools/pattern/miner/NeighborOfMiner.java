package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the "neighbor-of" relation.
 * @author Ozgun Babur
 */
public class NeighborOfMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets sif type.
	 */
	public NeighborOfMiner()
	{
		super(SIFEnum.NEIGHBOR_OF);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.neighborOf();
	}

	@Override
	public String getSourceLabel()
	{
		return "Protein 1";
	}

	@Override
	public String getTargetLabel()
	{
		return "Protein 2";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Inter"};
	}
}
