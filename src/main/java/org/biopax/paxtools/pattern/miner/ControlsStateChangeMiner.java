package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.constraint.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Miner for the controls-state-change pattern.
 * @author Ozgun Babur
 */
public class ControlsStateChangeMiner extends MinerAdapter
{
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
	 * Writes the result as "A tab B", where A and B are gene symbols, and tab is the tab character
	 * (\t).
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		// Memory for already written pairs.
		Set<String> mem = new HashSet<String>();

		OutputStreamWriter writer = new OutputStreamWriter(out);
		writer.write("upstream\tdownstream");

		for (BioPAXElement ele : matches.keySet())
		{
			for (Match m : matches.get(ele))
			{
				ProteinReference pr1 = (ProteinReference) m.get("controller ER", getPattern());
				ProteinReference pr2 = (ProteinReference) m.get("changed ER", getPattern());

				String s1 = getGeneSymbol(pr1);
				String s2 = getGeneSymbol(pr2);

				if (s1 != null && s2 != null)
				{
					String s = s1 + "\t" + s2;

					if (!mem.contains(s))
					{
						writer.write("\n" + s);
						mem.add(s);
					}
				}
			}
		}
	}
}
