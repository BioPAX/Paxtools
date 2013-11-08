package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Miner for the chemical-affects-protein pattern.
 * @author Ozgun Babur
 */
public class ChemicalAffectsThroughBindingMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * IDs of ubiquitous molecules.
	 */
	Set<String> ubiqueIDs = new HashSet<String>();

	/**
	 * Constructor that sets name and description.
	 */
	public ChemicalAffectsThroughBindingMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CHEMICAL_AFFECTS_PROTEIN.getTag(), "This pattern captures a small molecule" +
			"that appears in same complex with a protein.");

		this.ubiqueIDs = ubiqueIDs;
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.chemicalAffectsProteinThroughBinding(ubiqueIDs);
	}

	/**
	 * Writes the result as "A chemical-affect-protein B", where A is small molecule name, B is gene
	 * name, and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultAsSIF(matches, out, false, getSourceLabel(), getTargetLabel());
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
	public SIFType getSIFType(Match m)
	{
		return SIFType.CHEMICAL_AFFECTS_PROTEIN;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Complex"};
	}
}
