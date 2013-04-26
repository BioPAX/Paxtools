package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Miner for the controls-state-change pattern where the output is degraded.
 * @author Ozgun Babur
 */
public class AffectsDegradationMiner extends MinerAdapter
{
	/**
	 * Constructor that sets name and description.
	 */
	public AffectsDegradationMiner()
	{
		super("Affects-degradation", "This pattern finds relations where first protein " +
			"controls state change of the second protein, and the output PhysicalEntity of the " +
			"second protein is degraded. If the Control is positive the relation is DEGRADES, " +
			"else it is BLOCKS_DEGRADATION");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.controlsDegradationIndirectly();
	}

	/**
	 * Writes the result as "A DEGRADES B" or "A BLOCKS_DEGRADATION B", where A and B are gene
	 * symbols, and whitespace is tab.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		writeResultAsSIF(matches, out, true, "controller ER", "changed ER");
	}

	/**
	 * Sets header of the output.
	 * @return header
	 */
	@Override
	public String getHeader()
	{
		return "Upstream\ttype\tDownstream";
	}

	/**
	 * The relation can be either DEGRADES or BLOCKS_DEGRADATION.
	 * @param m the match
	 * @return type
	 */
	@Override
	public String getRelationType(Match m)
	{
		Control con = (Control) m.get("Control", getPattern());
		return con.getControlType() != null && con.getControlType().toString().startsWith("I") ?
			"BLOCKS_DEGRADATION" : "DEGRADES";
	}
}
