package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.util.Blacklist;

import java.util.HashSet;
import java.util.Set;

/**
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class CSCOThroughBindingSmallMoleculeMiner extends ControlsStateChangeOfMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public CSCOThroughBindingSmallMoleculeMiner(Blacklist blacklist)
	{
		super("-through-binding-small-mol", "The first protein produces a non-ubique small " +
			"molecule, and this small molecule controls state of the second protein.");

		super.blacklist = blacklist;
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChangeThroughBindingSmallMolecule(blacklist);
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"upper Control", "upper Conversion", "Conversion"};
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
