package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for the "related through interaction" relation.
 * @author Ozgun Babur
 */
public class RelatedThroughInteractionMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public RelatedThroughInteractionMiner()
	{
		super("Related-through-interaction", "This miner finds cases where two genes are " +
			"participants or controllers of the same interaction.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.relatedThroughInteraction();
	}

	/**
	 * Writes the result as "A related-through-interaction B", where A and B are gene symbols, and
	 * whitespace is tab.
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
		return "Protein 1";
	}

	@Override
	public String getTargetLabel()
	{
		return "Protein 2";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.RELATED_THROUGH_INTERACTION;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Inter"};
	}
}
