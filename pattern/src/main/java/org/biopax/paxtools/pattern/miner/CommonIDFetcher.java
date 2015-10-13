package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.HGNC;

import java.util.HashSet;
import java.util.Set;

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

	@Override
	public Set<String> fetchID(BioPAXElement ele)
	{
		Set<String> set = new HashSet<String>();

		if (ele instanceof SmallMoleculeReference || ele instanceof SmallMolecule)
		{
			Named e = (Named) ele;
			if (e.getDisplayName() != null)
				set.add(e.getDisplayName());
			else if (!e.getName().isEmpty())
				set.add(e.getName().iterator().next());

			return set; //TODO why now (what if SM/SMR has no names but has xrefs, e.g., ChEBI, Pubchem)?

		}
		else if (useUniprotIDs && ele.getUri().startsWith("http://identifiers.org/uniprot/"))
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
					if (!useUniprotIDs && db.startsWith("hgnc"))
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

		if (set.isEmpty() && ele instanceof Named) {
			Named e = (Named) ele;
			if (e.getDisplayName() != null)
				set.add(e.getDisplayName());
			else if (!e.getName().isEmpty())
				set.add(e.getName().iterator().next());
		}

		return set; //TODO what if it's still empty (some entities will be missing in the SIF output or not?)
	}

	public void setUseUniprotIDs(boolean useUniprotIDs)
	{
		this.useUniprotIDs = useUniprotIDs;
	}
}
