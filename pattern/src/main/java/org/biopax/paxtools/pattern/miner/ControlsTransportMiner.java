package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-transport pattern.
 * @author Ozgun Babur
 */
public class ControlsTransportMiner extends ControlsStateChangeOfMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsTransportMiner()
	{
		super(SIFEnum.CONTROLS_TRANSPORT_OF);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsTransport();
	}
}
