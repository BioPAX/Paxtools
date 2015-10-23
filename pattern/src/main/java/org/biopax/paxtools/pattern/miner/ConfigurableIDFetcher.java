package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.HGNC;

import java.util.HashSet;
import java.util.Set;

/**
 * Tries to get preferred IDs or names for entity references.
 * This id-fetcher can be optionally used when converting BioPAX to SIF.
 */
public class ConfigurableIDFetcher implements IDFetcher
{
	//TODO use Builder pattern (fluent API)

	private String dbStartsWith;
	private String dbEquals;


	/**
	 * Constructor.
	 */
	public ConfigurableIDFetcher() {
		dbStartsWith = "uniprot"; //default
		dbEquals = null;
	}

	/**
	 * Set to prefer collecting IDs of such Xrefs
	 * where the 'db' starts with given string,
	 * ignoring case.
	 *
	 * @param dbStartsWith
	 * @return this id-fetcher instance
	 */
	public ConfigurableIDFetcher dbStartsWith(String dbStartsWith) {
		this.dbStartsWith = dbStartsWith;
		return this;
	}

	/**
	 * Set to prefer collecting IDs of such Xrefs
	 * where the 'db' name equals given string,
	 * ignoring case.
	 *
	 * @param dbEquals
	 * @return this id-fetcher instance
	 */
	public ConfigurableIDFetcher dbEquals(String dbEquals) {
		this.dbEquals = dbEquals;
		return this;
	}



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

					if (!(ele instanceof SmallMoleculeReference) && db.startsWith("hgnc"))
					{//i.e., for PR and NucleicAcidReference
						String symbol = HGNC.getSymbol(id);
						if (symbol != null && !symbol.isEmpty())
						{
							set.add(symbol);
						}
					}
					else if (ele instanceof SequenceEntityReference &&
						(db.equals("mirbase sequence") || db.startsWith("ensg") || db.startsWith("enst")
							|| db.startsWith("entrez") || db.startsWith("ncbi gene") || db.startsWith("nucleotide")))
					{
						set.add(id);
					}
					else if (ele instanceof SmallMolecule && ((SmallMolecule) ele).getName().isEmpty() && db.startsWith("chebi"))
					{
						set.add(id);
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

		//still empty? - use URI
		if(set.isEmpty()) {
			set.add(ele.getUri());
		}

		return set;
	}

}
