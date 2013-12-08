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
 * Base class for SIF Miners.
 * @author Ozgun Babur
 */
public abstract class AbstractSIFMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * The binary interaction type that this class mines.
	 */
	SIFType type;

	/**
	 * Constructor with interaction type and ubique ids.
	 */
	public AbstractSIFMiner(SIFType type, Set<String> ubiqueIDs)
	{
		super(type.getTag(), type.getDescription(), ubiqueIDs);

		this.type = type;
	}

	/**
	 * Constructor with interaction type, supplementary name, supplementary description, and ubique
	 * ids. Supplementary name and description are sometimes needed because there can be multiple
	 * miners for the same binary interaction type. In that case these supplementary data is
	 * augmented to the name and description of the interaction type.
	 */
	public AbstractSIFMiner(SIFType type, String supplName, String supplDesc, Set<String> ubiqueIDs)
	{
		super(type.getTag() + supplName, type.getDescription() + " " + supplDesc, ubiqueIDs);

		this.type = type;
	}

	/**
	 * Writes the result as "A used-for-production-of B", where A and B are small molecule names,
	 * and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultAsSIF(matches, out, type.isDirected(), getSourceLabel(), getTargetLabel());
	}

	@Override
	public SIFType getSIFType()
	{
		return type;
	}
}
