package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.HGNC;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tries to get gene symbols or uniprot IDs for genes and display names for small molecules.
 *
 * @author Ozgun Babur
 */
public class CommonIDFetcher implements IDFetcher
{
	boolean useUniprotIDs = false;

	@Override
	public Set<String> fetchID(BioPAXElement ele)
	{
		Set<String> set = new HashSet<String>();

		if (ele instanceof SmallMoleculeReference)
		{
			SmallMoleculeReference smr = (SmallMoleculeReference) ele;
			if (smr.getDisplayName() != null) set.add(smr.getDisplayName());
			else if (!smr.getName().isEmpty()) set.add(smr.getName().iterator().next());

			return set;
		}
		else if (useUniprotIDs && ele.getRDFId().startsWith("http://identifiers.org/uniprot/"))
		{
			set.add(ele.getRDFId().substring(ele.getRDFId().lastIndexOf("/") + 1));
		}
		else if (ele instanceof XReferrable)
		{
			for (Xref xr : ((XReferrable) ele).getXref())
			{
				String db = xr.getDb();
				if (db != null)
				{
					db = db.toLowerCase();
					if (!useUniprotIDs && db.startsWith("hgnc"))
					{
						String id = xr.getId();
						if (id != null)
						{
							String symbol = HGNC.getSymbol(id);
							if (symbol != null && !symbol.isEmpty())
							{
								set.add(symbol);
							}
						}
					}
					else if (useUniprotIDs && db.startsWith("uniprot"))
					{
						String id = xr.getId();
						if (id != null)
						{
							set.add(id);
						}
					}
				}
			}
		}

		if (set.isEmpty() && (ele instanceof DnaReference || ele instanceof RnaReference))
		{
			for (Xref xr : ((XReferrable) ele).getXref())
			{
				String db = xr.getDb();
				if (db != null)
				{
					db = db.toLowerCase();

					if (db.equals("mirbase sequence"))
					{
						String id = xr.getId();
						if (id != null && !id.isEmpty()) set.add(id);
					}
				}
			}
		}

		return set;
	}

	public void setUseUniprotIDs(boolean useUniprotIDs)
	{
		this.useUniprotIDs = useUniprotIDs;
	}
}
