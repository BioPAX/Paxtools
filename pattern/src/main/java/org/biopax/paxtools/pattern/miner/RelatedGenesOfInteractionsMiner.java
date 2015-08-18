package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Miner for the related genes (participant or controller) of interactions.
 * @author Ozgun Babur
 */
public class RelatedGenesOfInteractionsMiner extends MinerAdapter
{
	/**
	 * Constructor that sets name and description.
	 */
	public RelatedGenesOfInteractionsMiner()
	{
		super("related-genes-of-interactions", "This miner finds any related gene that is a " +
			"participant or a controller of an Interaction (Conversion or TemplateReaction). " +
			"The output lists the ID of the interaction and the related gene symbols in a row.");
	}

	/**
	 * Constructs the pattern.
	 * @return pattern
	 */
	@Override
	public Pattern constructPattern()
	{
		return PatternBox.relatedProteinRefOfInter(
			Conversion.class,
			TemplateReaction.class);
	}

	private static final PathAccessor controlAcc = new PathAccessor("Interaction/controlledOf*");

	/**
	 * Writes the IDs of interaction, then gene symbols of related proteins in a line.
	 * @param matches pattern search result
	 * @param out output stream
	 */
	@Override
	public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
		throws IOException
	{
		OutputStreamWriter writer = new OutputStreamWriter(out);

		for (BioPAXElement ele : matches.keySet())
		{
			Set<String> syms = new HashSet<String>();

			for (Match m : matches.get(ele))
			{
				ProteinReference pr = (ProteinReference) m.get("PR", getPattern());

				String sym = getGeneSymbol(pr);
				if (sym != null) syms.add(sym);
			}

			if (syms.size() > 1)
			{
				writer.write("\n" + ele.getRDFId());

				for (Object o : controlAcc.getValueFromBean(ele))
				{
					Control ctrl = (Control) o;
					writer.write(" " + ctrl.getRDFId());
				}

				for (String sym : syms)
				{
					writer.write("\t" + sym);
				}
			}
		}
		writer.flush();
	}

}
