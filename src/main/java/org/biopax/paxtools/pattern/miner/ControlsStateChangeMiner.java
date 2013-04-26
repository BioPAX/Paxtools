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
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class ControlsStateChangeMiner extends MinerAdapter implements SIFMiner
{
	/**
	 * Constructor for extending purposes.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	public ControlsStateChangeMiner(String name, String description)
	{
		super(name, description);
	}

	/**
	 * Constructor that sets name and description.
	 */
	public ControlsStateChangeMiner()
	{
		super("Controls-state-change", "Finds relations between proteins where the first one is " +
			"controlling a reaction that changes the state of the second one. The reaction has " +
			"to be a Conversion and modified Protein should be represented with different " +
			"PhysicalEntity on each side. This pattern cannot determine the sign of the effect " +
			"because it is hard to predict the effect of the modification.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = PatternBox.controlsStateChange(true);
		p.addConstraint(new Type(ProteinReference.class), "controller ER");
		p.addConstraint(new Type(ProteinReference.class), "changed ER");
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
		return "Upstream\tDownstream";
	}

	@Override
	public String getSourceLabel()
	{
		return "controller ER";
	}

	@Override
	public String getTargetLabel()
	{
		return "changed ER";
	}

	@Override
	public String getRelationType(Match m)
	{
		return "state-change";
	}

	@Override
	public boolean isDirected()
	{
		return true;
	}
}
