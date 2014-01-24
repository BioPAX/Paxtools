package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Yet another miner for the controls-degradation pattern. This one searches two step relations.
 *
 * NOTE: THIS PATTERN DOES NOT WORK. KEEPING ONLY FOR HISTORICAL REASONS.
 *
 * @author Ozgun Babur
 */
public class ControlsDegradationIndirectMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsDegradationIndirectMiner()
	{
		super(SIFEnum.CONTROLS_DEGRADATION_OF, "-indirectly", "The control is to a previous " +
			"reaction that produces the degraded state of the protein.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsDegradationIndirectly();
	}

	@Override
	public String getSourceLabel()
	{
		return "controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed PR";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion", "degrading Conv"};
	}
}
