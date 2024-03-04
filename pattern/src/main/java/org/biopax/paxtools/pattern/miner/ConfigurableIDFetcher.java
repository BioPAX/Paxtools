package org.biopax.paxtools.pattern.miner;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.normalizer.Namespace;
import org.biopax.paxtools.normalizer.Resolver;
import org.biopax.paxtools.util.HGNC;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.*;

/**
 * Tries to get preferred type IDs of an entity reference.
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
		seqDbStartsWithOrEquals = new ArrayList<>();
		chemDbStartsWithOrEquals = new ArrayList<>();
		useNameWhenNoDbMatch = false;
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

	public List<String> getSeqDbStartsWithOrEquals() {
		return Collections.unmodifiableList(seqDbStartsWithOrEquals);
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

	public List<String> getChemDbStartsWithOrEquals() {
		return Collections.unmodifiableList(chemDbStartsWithOrEquals);
	}

	/**
	 * Set the flag to use the entity reference's names
	 * when no desired ID type can be found (none of xref.db
	 * matched before, or there were no xrefs at all).
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
		Set<String> set = new HashSet<>();
		if(ele instanceof XReferrable) {
			//Iterate the db priority list, match/filter all xrefs to collect the IDs of given type, until 'set' is not empty.
			List<String> dbStartsWithOrEquals =	(ele instanceof SmallMoleculeReference || ele instanceof SmallMolecule)
							? chemDbStartsWithOrEquals : seqDbStartsWithOrEquals;
			for (String dbStartsWith : dbStartsWithOrEquals) {
				for (UnificationXref x : new ClassFilterSet<>(((XReferrable) ele).getXref(),
						UnificationXref.class)) {
					collectXrefIdIfDbLike(x, dbStartsWith, set);
				}
				//if none were found in the unification xrefs, then try the relationship xrefs
				if (set.isEmpty()) {
					for (RelationshipXref x : new ClassFilterSet<>(((XReferrable) ele).getXref(),
							RelationshipXref.class)) {
						collectXrefIdIfDbLike(x, dbStartsWith, set);
					}
				}
				//once we've found some ID, no need to try another id type
				if (!set.isEmpty())
					break;
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
				Set<String> names = new TreeSet<>();
				for(String name : e.getName()) {
					if(!name.contains("..."))
						names.add(name);
				}
				set.add(names.toString());
			}
		}

		return set;
	}

	private void collectXrefIdIfDbLike(final Xref x, final String dbStartsWith, final Set<String> set) {
		String db = x.getDb();
		String id = x.getId();
		if (StringUtils.isNotBlank(db) && StringUtils.isNotBlank(id)) {
			//also find the bioregistry.io prefix and name if possible
			String dbName = "";
			String dbPrefix = "";
			String banana = "";
			String peel = "";
			if(Resolver.isKnownNameOrVariant(db)) {
				Namespace ns = Resolver.getNamespace(db);
			 	dbPrefix = ns.getPrefix();
			 	dbName = ns.getName();
				banana = ns.getBanana();
				peel = ns.getBanana_peel();
			}
			if (StringUtils.startsWithIgnoreCase(db, dbStartsWith)
					|| StringUtils.startsWithIgnoreCase(dbName, dbStartsWith)
					|| StringUtils.startsWithIgnoreCase(dbPrefix, dbStartsWith)) {
				//for a (PR/NAR) HGNC case, call HGNC mapping to the primary/current symbol
				if (StringUtils.startsWithIgnoreCase(db,"hgnc")) {
					id = HGNC.getSymbolByHgncIdOrSym(id);
				}
				if (id != null) {
					//e.g. for chebi IDs to have CHEBI: prefix
					if(StringUtils.isNotBlank(banana) && !id.startsWith(banana)) {
						id = banana+peel+id;
					}
					set.add(id); //match found
				}
			}
		}
	}

}
