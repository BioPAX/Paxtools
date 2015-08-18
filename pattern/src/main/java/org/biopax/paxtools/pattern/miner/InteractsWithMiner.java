package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the "interacts-with" relation.
 * @author Ozgun Babur
 */
public class InteractsWithMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets sif type.
	 */
	public InteractsWithMiner()
	{
		super(SIFEnum.INTERACTS_WITH);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.molecularInteraction();
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
		return new String[]{"MI"};
	}


	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"SPE1", "PE1"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"SPE2", "PE2"};
	}
}
