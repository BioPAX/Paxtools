package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.HGNC;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tries to get Gene Symbols or UniProt IDs for genes
 * and - display names for small molecules;
 *
 * This id-fetcher is mainly to use with the BioPAX pathway data
 * from Pathway Commons (PC2) db (normalized and enriched with xrefs).
 *
 * @author Ozgun Babur et al.
 */
public class CommonIDFetcher implements IDFetcher
{
	boolean useUniprotIDs = false;

	public Set<String> fetchID(BioPAXElement ele)
	{
		Set<String> set = new HashSet<>();

		if (ele instanceof SmallMoleculeReference || ele instanceof SmallMolecule)
		{
			Named e = (Named) ele;
			//avoid shortened/incomplete names -
			if (e.getDisplayName() != null && !e.getDisplayName().contains("..."))
				set.add(e.getDisplayName());
			else if (e.getStandardName() != null && !e.getStandardName().contains("..."))
				set.add(e.getStandardName());
			else if (!e.getName().isEmpty()) {
				Set<String> names = new TreeSet<>();
				for(String name : e.getName()) {
					if(!name.contains("..."))
						names.add(name);
				}
				set.add(names.toString());
			}
		}
		else if (useUniprotIDs &&
				(ele.getUri().contains("identifiers.org/uniprot") || ele.getUri().contains("bioregistry.io/uniprot")))
		{
			set.add(ele.getUri().substring(ele.getUri().lastIndexOf("/") + 1));
		}
		else if (ele instanceof XReferrable)
		{
			for (Xref xr : ((XReferrable) ele).getXref())
			{
				if(xr instanceof PublicationXref)
					continue;

				String db = xr.getDb();
				String id = xr.getId();
				if (db != null && id != null && !id.isEmpty())
				{
					db = db.toLowerCase();
					if (!useUniprotIDs && db.startsWith("hgnc")) //"hgnc.symbol" or "hgnc gene symbol"
					{
						//valid id is either a HGNC:1234 ID or Symbol (gene name)
						String symbol = HGNC.getSymbol(id);
						if (symbol != null && !symbol.isEmpty())
						{
							set.add(symbol);
						}
					}
					else if (useUniprotIDs && db.startsWith("uniprot"))
					{
						set.add(id);
					}
				}
			}
		}

		if (set.isEmpty()) {
			// a second try - getting mirtarbase ids for a nucleic acid...
			if (ele instanceof NucleicAcidReference || ele instanceof NucleicAcid)
			{
				for (Xref xr : ((XReferrable) ele).getXref()) {
					if(xr instanceof PublicationXref) continue;

					String db = xr.getDb();
					String id = xr.getId();
					if (db != null && id != null && !id.isEmpty()) {
						db = db.toLowerCase();
						if (db.equals("mirbase sequence")) {
							set.add(id);
						}
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
