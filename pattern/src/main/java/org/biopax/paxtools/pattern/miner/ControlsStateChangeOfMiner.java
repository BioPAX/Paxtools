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
	 * Constructor for extension purposes.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	protected ControlsStateChangeOfMiner(String name, String description)
	{
		super(SIFEnum.CONTROLS_STATE_CHANGE_OF, name, description);
	}

	/**
	 * Empty constructor.
	 */
	public ControlsStateChangeOfMiner()
	{
		super(SIFEnum.CONTROLS_STATE_CHANGE_OF);
	}

	/**
	 * Constructor for extension purposes.
	 * @param type relation type
	 */
	protected ControlsStateChangeOfMiner(SIFType type)
	{
		super(type);
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
