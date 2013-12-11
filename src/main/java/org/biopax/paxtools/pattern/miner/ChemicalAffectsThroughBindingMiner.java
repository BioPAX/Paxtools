package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.util.Blacklist;

import java.util.Set;

/**
 * Miner for the chemical-affects-protein pattern.
 * @author Ozgun Babur
 */
public class ChemicalAffectsThroughBindingMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ChemicalAffectsThroughBindingMiner(Blacklist blacklist)
	{
		super(SIFType.CHEMICAL_AFFECTS, "-through-binding", "In this case the chemical appears in" +
			" the same complex with the protein.", blacklist);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.chemicalAffectsProteinThroughBinding(blacklist);
	}

	@Override
	public String getSourceLabel()
	{
		return "SMR";
	}

	@Override
	public String getTargetLabel()
	{
		return "PR";
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Complex"};
	}
}
