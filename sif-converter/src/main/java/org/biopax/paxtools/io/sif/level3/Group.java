package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.impl.level3.EntityReferenceImpl;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Group extends EntityReferenceImpl
{
	Set<EntityReference> members = new HashSet<EntityReference>();

	Set<Group> subgroups = new HashSet<Group>();

	BinaryInteractionType type;

	Class<? extends BioPAXElement> genericType;

	Set<BioPAXElement> sources;

	Group(BinaryInteractionType type, BioPAXElement source)
	{
		this.type = type;
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

	public BinaryInteractionType getType()
	{
		return type;
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
			return this.type.equals(that.type) && this.members.equals(that.members) &&
			       this.subgroups.equals(that.subgroups);
		} else return false;
	}

	@Override public int hashCode()
	{
		int code;
		if (this.isEmpty())
		{
			code = sources.isEmpty() ? super.hashCode() : sources.hashCode();
		} else code = members.hashCode() / 17 + this.subgroups.hashCode() / 23 + this.getType().hashCode();
		return code;
	}

	public boolean isEmpty()
	{
		return this.members.isEmpty() && this.subgroups.isEmpty();
	}

	@Override public String toString()
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append(type).append("{");
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
		if(type == BinaryInteractionType.COMPONENT_OF)
		{
			return "ComplexGroup";
		}
		else if(type == BinaryInteractionType.GENERIC_OF)
		{
			return "Generic"+ (genericType==null?"":genericType.getSimpleName());
		}
		else throw new IllegalStateException();

	}
}
