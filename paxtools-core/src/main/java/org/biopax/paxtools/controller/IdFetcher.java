package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.*;

/**
 * Tries to get preferred type IDs of an entity reference.
 *
 * Could be used, e.g., as part of a BioPAX to plain text (or SIF, GMT) converter.
 */
public class IdFetcher
{
	private final List<String> seqDbStartsWithOrEquals;
	private final List<String> chemDbStartsWithOrEquals;
	private boolean useNameWhenNoDbMatch;

	/**
	 * Constructor.
	 */
	public IdFetcher() {
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
	public IdFetcher seqDbStartsWithOrEquals(String dbStartsWithOrEquals) {
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
	public IdFetcher chemDbStartsWithOrEquals(String dbStartsWithOrEquals) {
		this.chemDbStartsWithOrEquals.add(dbStartsWithOrEquals.toLowerCase());
		return this;
	}

	public List<String> getChemDbStartsWithOrEquals() {
		return Collections.unmodifiableList(chemDbStartsWithOrEquals);
	}

	/**
	 * Set the flag to use the entity reference's names
	 * when no desired ID type can be found (none of xref.db
	 * matched before, or there're no xrefs at all).
	 *
	 * @param useNameWhenNoDbMatch true/false (default is 'true' - when this method's never been called)
	 * @return this id-fetcher instance
	 */
	public IdFetcher useNameWhenNoDbMatch(boolean useNameWhenNoDbMatch) {
		this.useNameWhenNoDbMatch = useNameWhenNoDbMatch;
		return this;
	}

	public Set<String> fetchID(BioPAXElement ele)
	{
		Set<String> set = new HashSet<>();

		if(ele instanceof XReferrable) {
			//Iterate the db priority list, match/filter all xrefs to collect the IDs of given type, until 'set' is not empty.
			List<String> dbStartsWithOrEquals =
					(ele instanceof SmallMoleculeReference || ele instanceof SmallMolecule)
							? chemDbStartsWithOrEquals : seqDbStartsWithOrEquals;

			for (String dbStartsWith : dbStartsWithOrEquals) {
				//a shortcut for URI like "http://identifiers.org/uniprot/", "http://identifiers.org/chebi/";
				//this prevents collecting lots of secondary IDs of the same type
				if(ele.getUri().startsWith("http://identifiers.org/"+dbStartsWith)) {
					set.add(ele.getUri().substring(ele.getUri().lastIndexOf("/") + 1));
				}
				else
				{
					for (UnificationXref x : new ClassFilterSet<Xref, UnificationXref>(((XReferrable) ele).getXref(),
							UnificationXref.class)) {
						collectXrefIdIfDbLike(x, dbStartsWith, set);
					}
					//if none was found in unif. xrefs, try rel, xrefs
					if (set.isEmpty()) {
						for (RelationshipXref x : new ClassFilterSet<Xref, RelationshipXref>(((XReferrable) ele).getXref(),
								RelationshipXref.class)) {
							collectXrefIdIfDbLike(x, dbStartsWith, set);
						}
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

	private void collectXrefIdIfDbLike(final Xref x, final String dbStartsWith, final Set<String> set) {
		String db = x.getDb();
		String id = x.getId();
		if (db != null && id != null && !id.isEmpty()) {
			db = db.toLowerCase();
			if (db.startsWith(dbStartsWith)) {
				if (id != null)
					set.add(id);
			}
		}
	}

}
