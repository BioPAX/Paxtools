package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-transport pattern.
 * @author Ozgun Babur
 */
public class ControlsTransportMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsTransportMiner()
	{
		super(SIFType.CONTROLS_TRANSPORT_OF, null);
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
