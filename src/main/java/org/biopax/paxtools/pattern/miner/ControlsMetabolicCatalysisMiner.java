package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.pattern.constraint.ConBox.*;

/**
 * Miner for the controls-metabolic-catalysis pattern.
 * @author Ozgun Babur
 */
public class ControlsMetabolicCatalysisMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * IDs of ubiquitous molecules.
	 */
	Set<String> ubiqueIDs = new HashSet<String>();

	/**
	 * Constructor for extending purposes.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	public ControlsMetabolicCatalysisMiner(String name, String description)
	{
		super(name, description);
	}

	/**
	 * Constructor that sets name and description.
	 */
	public ControlsMetabolicCatalysisMiner(Set<String> ubiqueIDs)
	{
		super(SIFType.CONTROLS_METABOLIC_CATALYSIS.getTag(), "Finds relations from a controller " +
			"protein to a small molecule that is a participant of a reaction catalyzed by the " +
			"protein.");

		this.ubiqueIDs = ubiqueIDs;
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsMetabolicCatalysis(ubiqueIDs);
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
		return "Catalyzer\tRelation\tSmall molecule";
	}

	@Override
	public String getSourceLabel()
	{
		return "controller PR";
	}

	@Override
	public String getTargetLabel()
	{
		return "part SMR";
	}

	@Override
	public SIFType getSIFType(Match m)
	{
		return SIFType.CONTROLS_METABOLIC_CATALYSIS;
	}

	@Override
	public String[] getMediatorLabels()
	{
		return new String[]{"Control", "Conversion"};
	}
}
