package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Control;
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
 * Miner for the transcriptional regulation pattern.
 * @author Ozgun Babur
 */
public class TranscriptionalRegulationMiner extends MinerAdapter
{
	/**
	 * Constructor that sets name and description.
	 */
	public TranscriptionalRegulationMiner()
	{
		super("Transcriptional-regulation", "This pattern finds relations where first protein " +
			"is controlling transcriptional activity of the second protein. The output is either " +
			"\"A -> B\" or \"A -| B\", where -> represents trans-activation and -| represents " +
			"trans-inhibition. This pattern requires that transcription to be modeled with a " +
			"TemplateReaction.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		Pattern p = PatternBox.transcriptionWithTemplateReac();
		p.addConstraint(new Type(ProteinReference.class), "product ER");
		return p;
	}

	/**
	 * Writes the result as "A -> B" or "A -| B", where A and B are gene symbols, -> is
	 * transctivation, -| is transinhibition, and whitespace is tab.
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
		writer.write("Upstream\tRelation\tDownstream");

		for (BioPAXElement ele : matches.keySet())
		{
			for (Match m : matches.get(ele))
			{
				ProteinReference pr1 = (ProteinReference) m.get("TF PR", getPattern());
				ProteinReference pr2 = (ProteinReference) m.get("product ER", getPattern());

				Control con = (Control) m.get("Control", getPattern());
				String type = con.getControlType() != null &&
					con.getControlType().toString().startsWith("I") ? "-|" : "->";

				String s1 = getGeneSymbol(pr1);
				String s2 = getGeneSymbol(pr2);

				if (s1 != null && s2 != null)
				{
					String s = s1 + "\t" + type + "\t" + s2;

					if (!mem.contains(s))
					{
						writer.write("\n" + s);
						mem.add(s);
					}
				}
			}
		}

		writer.flush();
	}
}
