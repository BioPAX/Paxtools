package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

/**
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class ConStChThroughControllingSmallMoleculeMiner extends ControlsStateChangeMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ConStChThroughControllingSmallMoleculeMiner()
	{
		super("cont-st-ch-through-controlling-small-mol", "Mines the relation where the first" +
			"protein produces a non-ubique small molecule, and this small molecule controls" +
			"state of the second protein.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChangeThroughControllerSmallMolecule();
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"upper Control", "upper Conversion", "Control", "Conversion"};
	}

	@Override
	public String getSourceLabel()
	{
		return "upper controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed ER";
	}
}
