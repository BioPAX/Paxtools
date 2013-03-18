package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.util.HGNC;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
}
