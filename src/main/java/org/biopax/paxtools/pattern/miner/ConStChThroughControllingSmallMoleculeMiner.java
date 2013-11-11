package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.util.HashSet;
import java.util.Set;

/**
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class ConStChThroughControllingSmallMoleculeMiner extends ControlsStateChangeMiner
{
	/**
	 * IDs of ubiquitous molecules.
	 */
	Set<String> ubiqueIDs = new HashSet<String>();

	/**
	 * Constructor that sets name and description.
	 */
	public ConStChThroughControllingSmallMoleculeMiner(Set<String> ubiqueIDs)
	{
		super("cont-st-ch-through-controlling-small-mol", "Mines the relation where the first" +
			"protein produces a non-ubique small molecule, and this small molecule controls" +
			"state of the second protein.");

		this.ubiqueIDs = ubiqueIDs;
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChangeThroughControllerSmallMolecule(ubiqueIDs);
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
