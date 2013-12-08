package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the degradation pattern.
 * @author Ozgun Babur
 */
public class InComplexWithMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public InComplexWithMiner()
	{
		super(SIFType.IN_COMPLEX_WITH, null);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.appearInSameComplex();
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
		return new String[]{"Complex"};
	}
}
