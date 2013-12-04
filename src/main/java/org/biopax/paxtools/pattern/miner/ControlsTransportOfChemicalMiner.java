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
 * Miner for the controls-transport-of-chemical pattern.
 * @author Ozgun Babur
 */
public class ControlsTransportOfChemicalMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsTransportOfChemicalMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CONTROLS_TRANSPORT_OF_CHEMICAL.getTag(), "Finds relations from a protein to" +
			" a small molecule, where the protein controls a reaction that changes cellular " +
			"location of the small molecule.", ubiqueIDs);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.transportsChemical(ubiqueIDs);
	}

	/**
	 * Writes the result as "A controls-transport-of-chemical B", where A is gene symbol, B is small
	 * molecule name, and whitespace is tab.
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
		return "Upstream\tRelation\tDownstream";
	}

	@Override
	public String getSourceLabel()
	{
		return "controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed SMR";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONTROLS_TRANSPORT_OF_CHEMICAL;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
