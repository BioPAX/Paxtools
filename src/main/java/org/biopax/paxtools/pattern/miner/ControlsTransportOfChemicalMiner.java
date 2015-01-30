package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-transport-of-chemical pattern.
 * @author Ozgun Babur
 */
public class ControlsTransportOfChemicalMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsTransportOfChemicalMiner()
	{
		super(SIFEnum.CONTROLS_TRANSPORT_OF_CHEMICAL);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsTransportOfChemical(blacklist);
	}

	@Override
	public String getSourceLabel()
	{
		return "controller ER";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed SMR";
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
