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
 * Miner for the "neighbor-of" relation.
 * @author Ozgun Babur
 */
public class NeighborOfMiner extends AbstractSIFMiner
{
	/**
	 * Constructor that sets name and description.
	 */
	public NeighborOfMiner()
	{
		super(SIFType.NEIGHBOR_OF, null);
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.neighborOf();
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
	public String[] getMediatorLabels()
	{
		return new String[]{"Inter"};
	}
}
