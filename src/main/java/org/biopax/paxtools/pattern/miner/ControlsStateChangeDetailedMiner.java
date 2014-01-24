package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for the controls-state-change-detailed pattern. Different from the controls-state-change
 * pattern, this miner also records the modifications of the controller, and gained and lost
 * modifications of the changed gene.
 * @author Ozgun Babur
 */
public class ControlsStateChangeDetailedMiner extends MinerAdapter
{
	/**
	 * Constructor that sets name and description.
	 */
	public ControlsStateChangeDetailedMiner()
	{
		super("controls-state-change-detailed", "Captures exactly the same pattern as " +
			"\"Controls-state-change\", but the result file is more detailed. Together with " +
			"upstream and downstream genes, it also contains modifications of the upstream " +
			"entity, as well as the gained and lost modifications of the downstream entity.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsStateChange();
	}

	/**
	 * Writes the result as "A modifications-of-A B gains-of-B loss-of-B", where A and B are gene
	 * symbols, and whitespace is tab. Modifications are comma separated.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultDetailed(matches, out, 5);
	}

	/**
	 * Gets the header of the result file.
	 * @return header
	 */
	@Override
	public String getHeader()
	{
		return "Upstream\tModifications-of-upstream\tDownstream\tGain-of-downstream\tLoss-of-downstream";
	}

	/**
	 * Creates values for the result file columns.
	 * @param m current match
	 * @param col current column
	 * @return value of the given match at the given column
	 */
	@Override
	public String getValue(Match m, int col)
	{
		switch(col)
		{
			case 0:
			{
				return getGeneSymbol(m, "controller PR");
			}
			case 1:
			{
				return getModifications(m, "controller simple PE", "controller PE");
			}
			case 2:
			{
				return getGeneSymbol(m, "changed PR");
			}
			case 3:
			{
				return getDeltaModifications(m,
					"input simple PE", "input PE", "output simple PE", "output PE")[0];
			}
			case 4:
			{
				return getDeltaModifications(m,
					"input simple PE", "input PE", "output simple PE", "output PE")[1];
			}
			default: throw new RuntimeException("Invalid col number: " + col);
		}
	}
}
