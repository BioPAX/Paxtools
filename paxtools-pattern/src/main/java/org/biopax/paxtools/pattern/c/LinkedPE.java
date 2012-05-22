package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Gets related generics (member or parent) and either complexes or members recursively.
 *
 * @author Ozgun Babur
 */
public class LinkedPE extends ConstraintAdapter
{
	private Type type;
	
	private static PathAccessor upperGenAcc = new PathAccessor("PhysicalEntity/memberPhysicalEntityOf*");
	private static PathAccessor lowerGenAcc = new PathAccessor("PhysicalEntity/memberPhysicalEntity*");
	private static PathAccessor complexAcc = new PathAccessor("PhysicalEntity/componentOf*");
	private static PathAccessor memberAcc = new PathAccessor("Complex/component*");

	public LinkedPE(Type type)
	{
		this.type = type;
	}

	@Override
	public int getVariableSize()
	{
		return 2;
	}

	@Override
	public boolean canGenerate()
	{
		return true;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);
		Set<BioPAXElement> set = new HashSet<BioPAXElement>();
		set.add(pe);
		enrichWithGenerics(set, set);
		return set;
	}
	
	protected void enrichWithGenerics(Set<BioPAXElement> seed, Set<BioPAXElement> all)
	{
		Set addition = access(upperGenAcc, seed, all);
		addition.addAll(access(lowerGenAcc, seed, all));
		
		all.addAll(addition);
		seed.addAll(addition);

		enrichWithCM(seed, all);
	}

	protected void enrichWithCM(Set<BioPAXElement> seed, Set<BioPAXElement> all)
	{
		Set addition = access(type == Type.TO_COMPLEX ? complexAcc : memberAcc, seed, all);

		if (!addition.isEmpty())
		{
			all.addAll(addition);
			enrichWithGenerics(addition, all);
		}
	}
	
	protected Set access(PathAccessor pa, Set<BioPAXElement> seed, Set<BioPAXElement> all)
	{
		Set set = pa.getValueFromBeans(seed);
		set.removeAll(all);
		return set;
	}
	
	public enum Type
	{
		TO_COMPLEX,
		TO_MEMBER
	}
}
