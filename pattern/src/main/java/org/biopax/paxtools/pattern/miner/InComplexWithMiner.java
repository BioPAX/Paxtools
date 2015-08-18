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
	 * Constructor that sets edge type.
	 */
	public InComplexWithMiner()
	{
		super(SIFEnum.IN_COMPLEX_WITH);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.inComplexWith();
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

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"SPE1", "PE1", "Complex"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"SPE2", "PE2", "Complex"};
	}
}
