package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-phosphorylation pattern.
 * @author Ozgun Babur
 */
public class ControlsPhosphorylationMiner extends ControlsStateChangeOfMiner
{
	/**
	 * Constructor that sets the type.
	 */
	public ControlsPhosphorylationMiner()
	{
		super("phosphorylation",
			SIFEnum.CONTROLS_PHOSPHORYLATION_OF.getDescription());
		setType(SIFEnum.CONTROLS_PHOSPHORYLATION_OF);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsPhosphorylation();
	}
}
