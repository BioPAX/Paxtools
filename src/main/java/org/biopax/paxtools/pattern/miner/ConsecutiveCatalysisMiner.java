package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Miner for the consecutive-catalysis pattern.
 * @author Ozgun Babur
 */
public class ConsecutiveCatalysisMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * IDs of ubiquitous molecules.
	 */
	Set<String> ubiqueIDs = new HashSet<String>();

	/**
	 * Constructor that sets name and description.
	 */
	public ConsecutiveCatalysisMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CONSECUTIVE_CATALYSIS.getTag(), "Finds two proteins, catalyzing different " +
			"reactions, where an output of first reaction is input to second reaction.");

		this.ubiqueIDs = ubiqueIDs;
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = PatternBox.consecutiveCatalysis(ubiqueIDs);
		p.addConstraint(new Type(ProteinReference.class), "first ER");
		p.addConstraint(new Type(ProteinReference.class), "second ER");
		return p;
	}

	/**
	 * Writes the result as "A B", where A and B are gene symbols, and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultAsSIF(matches, out, true, getSourceLabel(), getTargetLabel());
	}

	/**
	 * Sets header of the output.
	 * @return header
	 */
	@Override
	public String getHeader()
	{
		return "First-protein\tRelation\tSecond-protein";
	}

	@Override
	public String getSourceLabel()
	{
		return "first ER";
	}

	@Override
	public String getTargetLabel()
	{
		return "second ER";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONSECUTIVE_CATALYSIS;
	}

	@Override
	public String[] getPubmedHarvestableLabels()
	{
		return new String[]{"first Control", "first Conversion", "second Control",
			"second Conversion"};
	}
}
