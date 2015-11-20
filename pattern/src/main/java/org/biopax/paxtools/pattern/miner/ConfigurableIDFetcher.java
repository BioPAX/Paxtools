package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.HGNC;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;

/**
 * Tries to get preferred type IDs, or a name, or URI (at last)
 * of an entity reference.
 *
 * This id-fetcher can be optionally used
 * when converting (reducing) BioPAX to the binary SIF format.
 */
public class ConfigurableIDFetcher implements IDFetcher
{
	private final List<String> seqDbStartsWithOrEquals;
	private final List<String> chemDbStartsWithOrEquals;
	private boolean useNameWhenNoDbMatch;

	/**
	 * Constructor.
	 */
	public ConfigurableIDFetcher() {
		seqDbStartsWithOrEquals = new ArrayList<String>();
		chemDbStartsWithOrEquals = new ArrayList<String>();
		useNameWhenNoDbMatch = true;
	}

	/**
	 * Set to prefer collecting gene/sequence IDs of such Xrefs
	 * where the db starts with or equals given string,
	 * ignoring case. You can chain this method calls like
	 * seqDbStartsWithOrEquals(A).seqDbStartsWithOrEquals(B)... -
	 * it will try to match a xref.db and collect xref.id
	 * in the given order/priority.
	 *
	 * @param dbStartsWithOrEquals the Xref.db value or prefix (case-insensitive)
	 * @return this id-fetcher instance
	 */
	public ConfigurableIDFetcher seqDbStartsWithOrEquals(String dbStartsWithOrEquals) {
		this.seqDbStartsWithOrEquals.add(dbStartsWithOrEquals.toLowerCase());
		return this;
	}

	/**
	 * Set to prefer collecting chemical IDs of such Xrefs
	 * where the small molecules db starts with or equals given string,
	 * ignoring case. You can chain this method calls like
	 * chemDbStartsWithOrEquals(A).chemDbStartsWithOrEquals(B)... -
	 * it will try to match a xref.db and collect xref.id
	 * in the given order/priority.
	 *
	 * @param dbStartsWithOrEquals the Xref.db value or prefix (case-insensitive)
	 * @return this id-fetcher instance
	 */
	public ConfigurableIDFetcher chemDbStartsWithOrEquals(String dbStartsWithOrEquals) {
		this.chemDbStartsWithOrEquals.add(dbStartsWithOrEquals.toLowerCase());
		return this;
	}

	/**
	 * Set the flag to use the entity reference's names
	 * when no desired ID type can be found (none of xref.db
	 * matched before, or there're no xrefs at all).
	 *
	 * @param useNameWhenNoDbMatch true/false (default is 'true' - when this method's never been called)
	 * @return this id-fetcher instance
	 */
	public ConfigurableIDFetcher useNameWhenNoDbMatch(boolean useNameWhenNoDbMatch) {
		this.useNameWhenNoDbMatch = useNameWhenNoDbMatch;
		return this;
	}

	public Set<String> fetchID(BioPAXElement ele)
	{
		Set<String> set = new HashSet<String>();

//		if(!(ele instanceof EntityReference || ele instanceof PhysicalEntity || ele instanceof Gene)) {
//			throw new IllegalBioPAXArgumentException("fetchID, unsupported type: "
//					+ ele.getUri() + " is not a ER/PE/Gene but " + ele.getModelInterface().getSimpleName());
//		}

		if(ele instanceof XReferrable) {
			//Iterate the db priority list, match/filter all xrefs to collect the IDs of given type, until 'set' is not empty.
			List<String> dbStartsWithOrEquals =
					(ele instanceof SmallMoleculeReference || ele instanceof SmallMoleculeReference)
							? chemDbStartsWithOrEquals : seqDbStartsWithOrEquals;

			for (String dbStartsWith : dbStartsWithOrEquals) {
				for (Xref x : ((XReferrable)ele).getXref()) //interface Named extends XReferrable
				{
					//skip for PublicationXref
					if (x instanceof PublicationXref) continue;

					String db = x.getDb();
					String id = x.getId();

					if (db != null && id != null && !id.trim().isEmpty()) {
						db = db.toLowerCase();
						if (db.startsWith(dbStartsWith)) {
							//for a (PR/NAR) HGNC case, call HGNC.getSymbol(id) mapping
							if (db.startsWith("hgnc"))
								id = HGNC.getSymbol(id);

							if (id != null && !id.isEmpty())
								set.add(id);
						}
					}
				}
				if (!set.isEmpty())
					break; //we collected all the IDs of a kind; no need to try alternative prefixes from the rest of the list
			}
		}

		if (set.isEmpty() && ele instanceof Named && useNameWhenNoDbMatch)
		{
			Named e = (Named) ele;
			//avoid shortened/incomplete names -
			if (e.getDisplayName() != null && !e.getDisplayName().contains("..."))
				set.add(e.getDisplayName());
			else if (e.getStandardName() != null && !e.getStandardName().contains("..."))
				set.add(e.getStandardName());
			else if (!e.getName().isEmpty()) {
				Set<String> names = new TreeSet<String>();
				for(String name : e.getName()) {
					if(!name.contains("..."))
						names.add(name);
				}
				set.add(names.toString());
			}
		}

		return set;
	}
}
