package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.HGNC;

import java.util.HashSet;
import java.util.Set;

/**
 * Tries to get display names for molecules and genes;
 *
 * This id-fetcher can be used in Cytoscape3 BioPAX reader app
 * when converting BioPAX to SIF.
 *
 */
public class NamedIDFetcher implements IDFetcher
{

	//TODO add a 'xrefDb' field

	@Override
	public Set<String> fetchID(BioPAXElement ele)
	{
		Set<String> set = new HashSet<String>();

		if(ele instanceof XReferrable) {
			for (Xref xr : ((XReferrable) ele).getXref())
			{
				if(xr instanceof PublicationXref)
					continue;

				String db = xr.getDb();
				String id = xr.getId();
				if (db != null && id != null && !id.isEmpty())
				{
					db = db.toLowerCase();

					if (!(ele instanceof SmallMolecule) && db.startsWith("hgnc"))
					{
						String symbol = HGNC.getSymbol(id);
						if (symbol != null && !symbol.isEmpty())
						{
							set.add(symbol);
						}
					}
					else if ((ele instanceof NucleicAcid || ele instanceof NucleicAcidReference)
						&& ((Named)ele).getName().isEmpty()
							&& (db.equals("mirbase sequence") || db.startsWith("ensg") || db.startsWith("enst")
							|| db.startsWith("entrez") || db.startsWith("ncbi") || db.startsWith("nucleotide")))
					{
						set.add(id);
					}
					else if (ele instanceof SmallMolecule && ((SmallMolecule) ele).getName().isEmpty() &&
							(db.startsWith("chebi") || db.startsWith("pubchem")))
					{
						//id is either a HGNC:1234 ID or Symbol (gene name)
						String symbol = HGNC.getSymbol(id);
						if (symbol != null && !symbol.isEmpty())
						{
							set.add(symbol);
						}
					}
				}
			}
		}

		if (set.isEmpty() && ele instanceof Named)
		{
			Named named = (Named) ele;

			if (named.getDisplayName() != null)
				set.add(named.getDisplayName());
			else if (!named.getName().isEmpty())
				set.add(named.getName().iterator().next());
		}

		//TODO what if the set is still empty?
		if(set.isEmpty()) {
			set.add(ele.getUri());
		}

		return set;
	}

}
