package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.util.HGNC;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Adapter class for a miner.
 *
 * @author Ozgun Babur
 */
public abstract class MinerAdapter implements Miner
{
	/**
	 * Name of the miner.
	 */
	protected String name;

	/**
	 * Description of the miner.
	 */
	protected String description;

	/**
	 * Pattern to use for mining.
	 */
	protected Pattern pattern;

	/**
	 * Accessor to the xref of EntityReference.
	 */
	private static final Set<String> symbolNames = new HashSet<String>(Arrays.asList("hgnc"));

	/**
	 * Constructor with name and description.
	 * @param name name of the miner
	 * @param description description of the miner
	 */
	protected MinerAdapter(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	/**
	 * Constructs the pattern to use for mining.
	 * @return the pattern
	 */
	public abstract Pattern constructPattern();

	/**
	 * Gets the pattern, constructs if null.
	 * @return pattern
	 */
	public Pattern getPattern()
	{
		if (pattern == null) pattern = constructPattern();

		return pattern;
	}

	/**
	 * Gets the name of the miner.
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the description of the miner.
	 * @return description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Searches for the gene symbol of the given EntityReference.
	 * @param er to search for a symbol
	 * @return symbol
	 */
	protected String getGeneSymbol(EntityReference er)
	{
		for (Xref xr : er.getXref())
		{
			String db = xr.getDb();
			if (db != null)
			{
				db = db.toLowerCase();
				if (symbolNames.contains(db))
				{
					String id = xr.getId();
					if (id != null)
					{
						String symbol = HGNC.getSymbol(id);
						if (symbol != null && !symbol.isEmpty())
						{
							return symbol;
						}
					}
				}
			}
		}

//		return er.getDisplayName();
		return null;
	}

	/**
	 * Uses the name as sting representation of the miner.
	 * @return name
	 */
	@Override
	public String toString()
	{
		return getName();
	}

	/**
	 * Checks if the type of a Control is inhibition.
	 * @param ctrl Control to check
	 * @return true if type is inhibition related
	 */
	public boolean isInhibition(Control ctrl)
	{
		return ctrl.getControlType() != null && ctrl.getControlType().toString().startsWith("I");
	}

	/**
	 * This method writes the output as pairs of gene symbols of the given two ProteinReference.
	 * Parameters labels have to map to ProteinReference.
	 * @param matches the search result
	 * @param out output stream for text output
	 * @param directed if true, reverse pairs is treated as different pairs
	 * @param label1 label for the first ProteinReference in the result matches
	 * @param label2 label for the second ProteinReference in the result matches
	 * @throws IOException if cannot write to output stream
	 */
	public void writeResultAsPair(Map<BioPAXElement, List<Match>> matches, OutputStream out,
		boolean directed, String label1, String label2) throws IOException
	{
		// Memory for already written pairs.
		Set<String> mem = new HashSet<String>();

		OutputStreamWriter writer = new OutputStreamWriter(out);
		writer.write(label1 + "\t" + label2);

		for (BioPAXElement ele : matches.keySet())
		{
			for (Match m : matches.get(ele))
			{
				ProteinReference pr1 = (ProteinReference) m.get(label1, getPattern());
				ProteinReference pr2 = (ProteinReference) m.get(label2, getPattern());

				String s1 = getGeneSymbol(pr1);
				String s2 = getGeneSymbol(pr2);

				if (s1 != null && s2 != null)
				{
					String relation = s1 + "\t" + s2;
					String reverse = s2 + "\t" + s1;

					if (!mem.contains(relation) && (directed || !mem.contains(reverse)))
					{
						writer.write("\n" + relation);
						mem.add(relation);
						if (!directed) mem.add(reverse);
					}
				}
			}
		}
		writer.flush();
	}
}
