package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.util.Set;

/**
 * Miner for the chemical-affects-protein pattern.
 * @author Ozgun Babur
 */
public class ChemicalAffectsThroughControlMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ChemicalAffectsThroughControlMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CHEMICAL_AFFECTS, "-through-control", "In this case, chemical is " +
			"controlling a reaction of which the protein is a participant.", ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.chemicalAffectsProteinThroughControl(ubiqueIDs);
	}

	@Override
	public String getSourceLabel()
	{
		return "controller SMR";
	}

	@Override
	public String getTargetLabel()
	{
		return "affected PR";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Interaction"};
	}
}
