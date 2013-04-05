package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
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
 * Miner for the degradation pattern.
 * @author Ozgun Babur
 */
public class DegradesMiner extends MinerAdapter
{
	/**
	 * Constructor that sets name and description.
	 */
	public DegradesMiner()
	{
		super("Degrades-or-blocks-it", "This pattern finds relations where first protein " +
			"is controlling a Conversion that degrades the second protein. If the Control is " +
			"positive the relation is DEGRADES, else it is BLOCKS_DEGRADATION");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.degradation();
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
		getPattern().updateLabel("upstream PR", "Upstream");
		getPattern().updateLabel("downstream PR", "Downstream");
		writeResultAsSIF(matches, out, true, "Upstream", "Downstream");
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
