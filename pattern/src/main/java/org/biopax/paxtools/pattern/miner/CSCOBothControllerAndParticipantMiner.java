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
		super(SIFEnum.CONTROLS_STATE_CHANGE_OF, "-both-ctrl-part", "The controller is also an " +
			"input. The reaction has to be a Conversion and modified Protein should be " +
			"represented with different non-generic PhysicalEntity on each side.");
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
		return "controller ER";
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

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"controller simple PE", "controller PE"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"input PE", "input simple PE", "output PE", "output simple PE"};
	}
}
