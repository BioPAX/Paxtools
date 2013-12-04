package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Miner for the consumption-controlled-by pattern.
 * @author Ozgun Babur
 */
public class ConsumptionControlledByMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ConsumptionControlledByMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CONSUMPTION_CONTROLLED_BY.getTag(), "Finds relations from a small molecule " +
			"that is consumed by a reaction, to a protein that controls this reaction.", ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.meabolicCatalysisSubclass(ubiqueIDs, true);
	}

	/**
	 * Writes the result as "A consumption-controlled-by B", where A is small molecule name, B is
	 * gene symbol, and whitespace is tab.
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
		return "Chemical\tRelation\tProtein";
	}

	@Override
	public String getSourceLabel()
	{
		return "part SMR";
	}

	@Override
	public String getTargetLabel()
	{
		return "controller PR";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONSUMPTION_CONTROLLED_BY;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
