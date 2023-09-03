package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Many times we want to link PhysicalEntities (PE) while traversing a relation. For instance when
 * we have a PE and want to go the Interactions that it participates, we may also want to consider
 * the complexes that the PE is in. Or we also want to traverse homologies (parent or member PEs)
 * This generative constraint gets related generics (member or parent) and either complexes or
 * members recursively.
 *
 * Linking process is a bit confusing. There two modes: TO_COMPLEX and TO_MEMBER. Linking homologies
 * is identical for both cases. IT is either parent PEs recursively, or member PEs recursively. But
 * not parent of a member (recursion do not alternate between parent and member).
 *
 * In TO_COMPLEX mode, only parent complexes are linked recursively, and in TO_MEMBER mode, only the
 * complex member PEs are linked recursively.
 *
 * Homology linking and complex-relation linking can alternate recursively.
 *
 * @author Ozgun Babur
 */
public class LinkedPE extends ConstraintAdapter
{
	/**
	 * Type of the linking.
	 */
	private Type type;

	/**
	 * Accessor to get parent PhysicalEntity.
	 */
	private static PathAccessor upperGenAcc = new PathAccessor("PhysicalEntity/memberPhysicalEntityOf*");

	/**
	 * Accessor to get member PhysicalEntity.
	 */
	private static PathAccessor lowerGenAcc = new PathAccessor("PhysicalEntity/memberPhysicalEntity*");

	/**
	 * Accessor to get parent Complex.
	 */
	private static PathAccessor complexAcc = new PathAccessor("PhysicalEntity/componentOf*");

	/**
	 * Accessor to get complex members.
	 */
	private static PathAccessor memberAcc = new PathAccessor("Complex/component*");

	/**
	 * Constructor with the linking type.
	 * @param type type of desired linking
	 */
	public LinkedPE(Type type)
	{
		this(type, null);
	}

	/**
	 * Constructor with the linking type.
	 * @param type type of desired linking
	 * @param blacklist a skip-list of ubiquitous molecules
	 */
	public LinkedPE(Type type, Blacklist blacklist)
	{
		super(2, blacklist);
		this.type = type;
	}

	/**
	 * This is a generative constraint.
	 * @return true
	 */
	@Override
	public boolean canGenerate()
	{
		return true;
	}

	/**
	 * Gets to the linked PhysicalEntity.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return linked PhysicalEntity
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);
		Set<BioPAXElement> set = getLinkedElements(pe);

		return set;
	}

	public Set<BioPAXElement> getLinkedElements(PhysicalEntity pe)
	{
		Set<BioPAXElement> set = new HashSet<>();
		set.add(pe);
		enrichWithGenerics(set, set);
		return set;
	}

	/**
	 * Gets the linked homologies and then switches to complex-relationship mode. These two enrich
	 * methods call each other recursively.
	 * @param seed to get the linked elements from
	 * @param all already found links
	 */
	protected void enrichWithGenerics(Set<BioPAXElement> seed, Set<BioPAXElement> all)
	{
		Set addition;
		if (type == Type.TO_GENERAL) addition = access(upperGenAcc, seed, all);
		else addition = access(lowerGenAcc, seed, all);
		
		all.addAll(addition);
		seed.addAll(addition);

		enrichWithCM(seed, all);
	}

	/**
	 * Gets parent complexes or complex members recursively according to the type of the linkage.
	 * @param seed elements to link
	 * @param all already found links
	 */
	protected void enrichWithCM(Set<BioPAXElement> seed, Set<BioPAXElement> all)
	{
		Set addition = access(type == Type.TO_GENERAL ? complexAcc : memberAcc, seed, all);

		if (blacklist != null) addition = blacklist.getNonUbiqueObjects(addition);

		if (!addition.isEmpty())
		{
			all.addAll(addition);
			enrichWithGenerics(addition, all);
		}
	}

	/**
	 * Uses the given PathAccessor to access fields of the seed and return only new elements that is
	 * not in the given element set (all).
	 * @param pa accessor to the filed
	 * @param seed entities to get their fields
	 * @param all already found values
	 * @return new values
	 */
	protected Set access(PathAccessor pa, Set<BioPAXElement> seed, Set<BioPAXElement> all)
	{
		Set set = pa.getValueFromBeans(seed);
		set.removeAll(all);

		// remove blacklisted
		if (blacklist == null) return set;
		else return blacklist.getNonUbiqueObjects(set);
	}

	/**
	 * Two type of linking between PhysicalEntity.
	 */
	public enum Type
	{
		TO_GENERAL,
		TO_SPECIFIC
	}
}
