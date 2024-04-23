package org.biopax.paxtools.pattern.miner;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.normalizer.Resolver;
import org.biopax.paxtools.util.HGNC;

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
				(StringUtils.containsIgnoreCase(ele.getUri(),"identifiers.org/uniprot")
						|| StringUtils.containsIgnoreCase(ele.getUri(),"bioregistry.io/uniprot")))
		//can be like ...identifiers.org/uniprot:AC or identifiers.org/uniprot/AC
		// or http://bioregistry.io/uniprot:AC or bioregistry.io/uniprot.isoform:...
		{
			String ac = StringUtils.substringAfterLast(ele.getUri(), "/");
			if(StringUtils.contains(ac, ":")) {
				ac = StringUtils.substringAfter(ac, ":");
			}
			set.add(ac);
		}
		else if (ele instanceof XReferrable)
		{
			for (Xref xr : ((XReferrable) ele).getXref())
			{
				if(xr instanceof PublicationXref)
					continue;

				String db = xr.getDb();
				String id = xr.getId();
				if (StringUtils.isNotBlank(db) && StringUtils.isNotBlank(id))
				{
					db = db.toLowerCase();
					//find the official collection/db name and prefix
					if(Resolver.isKnownNameOrVariant(db)) {
						db = Resolver.getNamespace(db).getPrefix();
					}
					if (!useUniprotIDs && db.startsWith("hgnc")) //"hgnc.symbol", "hgnc" or "hgnc symbol", etc.
					{
						//valid id is either HGNC:1234 ID or 1234 or Symbol (gene name, former name)
						String symbol = HGNC.getSymbolByHgncIdOrSym(id);
						if (StringUtils.isNotEmpty(symbol)) {
							set.add(symbol);
						}
					}
					else if (useUniprotIDs && db.startsWith("uniprot")) {
						set.add(id);
					}
				}
			}
		}

		if (set.isEmpty()) {
			// a second try - getting mirbase ids for a nucleic acid...
			if (ele instanceof NucleicAcidReference || ele instanceof NucleicAcid)
			{
				for (Xref xr : ((XReferrable) ele).getXref()) {
					if(xr instanceof PublicationXref)
						continue;

					String db = xr.getDb();
					String id = xr.getId();
					if (StringUtils.isNotBlank(db) && StringUtils.isNotBlank(id)) {
						//find the official collection/db name and prefix
						if(Resolver.isKnownNameOrVariant(db)) {
							db = Resolver.getNamespace(db).getPrefix();
						}
						if (StringUtils.equalsAnyIgnoreCase(db, "mirbase", "mirbase sequence")) {
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
