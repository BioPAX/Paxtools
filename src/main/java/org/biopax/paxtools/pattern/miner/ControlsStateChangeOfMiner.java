package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class ControlsStateChangeOfMiner extends AbstractSIFMiner
{
	/**
	 * Constructor for extending purposes.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	public ControlsStateChangeOfMiner(String name, String description)
	{
		super(SIFEnum.CONTROLS_STATE_CHANGE_OF, name, description);
	}

	/**
	 * Constructor that sets name and description.
	 */
	public ControlsStateChangeOfMiner()
	{
		super(SIFEnum.CONTROLS_STATE_CHANGE_OF);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChange();
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
		return new String[]{"Control", "Conversion"};
	}
}
