package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-state-change pattern. This time the controller is also an input.
 * @author Ozgun Babur
 */
public class CSCOBothControllerAndParticipantMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public CSCOBothControllerAndParticipantMiner()
	{
		super(SIFType.CONTROLS_STATE_CHANGE_OF, "-both-ctrl-part", "The controller is also an " +
			"input. The reaction has to be a Conversion and modified Protein should be " +
			"represented with different non-generic PhysicalEntity on each side.", null);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChangeBothControlAndPart();
	}

	@Override
	public String getSourceLabel()
	{
		return "controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed ER";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
