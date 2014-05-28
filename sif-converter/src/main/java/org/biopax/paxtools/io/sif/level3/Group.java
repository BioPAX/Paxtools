package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.impl.level3.EntityReferenceImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a grouping of EntityReferences strictly used by SIF rules for handling generics and complexes.
 *
 * @author Emek Demir
 */
public class Group extends EntityReferenceImpl
{
	/**
	 * ER members of the group.
	 */
	Set<EntityReference> members = new HashSet<EntityReference>();

	/**
	 *  Since Group is also an ER, its members can be arbitrarily nested.
	 */
	Set<Group> subgroups = new HashSet<Group>();

	/**
	 * This value is true if this group maps to a complex, false otherwise.
	 */
	boolean isComplex;

	/**
	 * The class of the sources that this group maps
	 */
	Class<? extends BioPAXElement> genericClass;

	/**
	 * The original bpes that this group maps.
	 */
	Set<BioPAXElement> sources;


	/**
	 *
	 * @param type This value is true if this group maps to a complex, false otherwise.
	 * @param source The original bpes that this group maps.
	 */
	Group(boolean type, BioPAXElement source)
	{
		this.isComplex = type;
		this.sources = new HashSet<BioPAXElement>();
		this.sources.add(source);
	}

	@Override public String getRDFId()
	{
		return "http://biopax.org/generated/group/" + hashCode();
	}

	@Override public boolean isEquivalent(BioPAXElement element)
	{
		return this.equals(element);
	}

	@Override public int equivalenceCode()
	{
		return hashCode();
	}


	public void addMember(EntityReference member)
	{
		this.members.add(member);
	}

	public void addSubgroup(Group subgroup)
	{
		if(subgroup!=this)
		this.subgroups.add(subgroup);
		else
			throw new IllegalArgumentException();
	}

	public boolean isComplex()
	{
		return isComplex;
	}

	@Override public boolean equals(Object o)
	{
		if (o == this) return true;
		if (o != null && o instanceof Group)
		{
			Group that = (Group) o;

			if (this.isEmpty())
			{
				return this.sources.equals(that.sources);
			}
			return this.isComplex == that.isComplex && this.members.equals(that.members) &&
			       this.subgroups.equals(that.subgroups);
		} else return false;
	}

	@Override public int hashCode()
	{
		int code;
		if (this.isEmpty())
		{
			code = sources.isEmpty() ? super.hashCode() : sources.hashCode();
		} else code = members.hashCode() / 17 + this.subgroups.hashCode() / 23 + (isComplex?19:47);
		return code;
	}

	public boolean isEmpty()
	{
		return this.members.isEmpty() && this.subgroups.isEmpty();
	}

	@Override public String toString()
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append(isComplex).append("{");
		if (!isEmpty())
		{
			for (EntityReference member : members)
			{
				bldr.append(member.getRDFId()).append(",");
			}
			for (Group subgroup : subgroups)
			{
				bldr.append(subgroup.toString()).append(",");
			}

			bldr.deleteCharAt(bldr.length() - 1);
		}
		return bldr.append("}").toString();

	}

	@Override
	public Map<String, Object> getAnnotations() {
		throw new UnsupportedOperationException("getAnnotations() is not supported " +
			"for this (special) BioPAXElement: " + getClass());
	}

	public String groupTypeToString()
	{
		if(isComplex)
		{
			return "ComplexGroup";
		}
		else
		{
			return "Generic"+ (genericClass ==null?"": genericClass.getSimpleName());
		}

	}

	public Set<EntityReference> getAllSimpleMembers()
	{
		HashSet simples = new HashSet<EntityReference>();
		recursivelyGetMembers(simples);
		return simples;
	}

	private void recursivelyGetMembers(Set<EntityReference> simples)
	{
		for (EntityReference member : members)
		{
			simples.add(member);
		}
		for (Group subgroup : subgroups)
		{
			subgroup.recursivelyGetMembers(simples);
		}
	}

}
