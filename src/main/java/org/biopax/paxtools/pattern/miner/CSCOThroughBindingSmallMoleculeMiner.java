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
	public CSCOThroughBindingSmallMoleculeMiner()
	{
		super("-through-binding-small-mol", "The first protein produces a non-ubique small " +
			"molecule, and this small molecule controls state of the second protein.");
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
		return new String[]{"upper Control", "upper Conversion", "Conversion", "SM"};
	}

	@Override
	public String getSourceLabel()
	{
		return "upper controller ER";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed ER";
	}

	@Override
	public String[] getSourcePELabels()
	{
		return new String[]{"upper controller simple PE", "upper controller PE"};
	}

	@Override
	public String[] getTargetPELabels()
	{
		return new String[]{"input PE", "input simple PE", "output PE", "output simple PE"};
	}
}
