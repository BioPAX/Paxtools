package org.biopax.paxtools.impl.level3;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.*;

/**
 */
@Entity
abstract class NamedImpl extends XReferrableImpl
{

	private String standardName;
	private String displayName;
	private Set<String> allNames;

	NamedImpl()
	{
		allNames = new HashSet<String>();
		displayName = null;
		standardName = null;
	}

	@Basic
	public String getStandardName()
	{
		return standardName;
	}

	public void setStandardName(String name)
	{
		addName(standardName = name);
	}

	@Basic
	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String name)
	{
		addName(displayName = name);
	}

	@ElementCollection
	public Set<String> getName()
	{
		return allNames;
	}

	public void setName(Set<String> names)
	{
		allNames = names;
		if (!names.contains(standardName))
		{
			standardName = null;
		}
		if (!names.contains(displayName))
		{
			displayName = null;
		}
	}

	public void addName(String name)
	{
		allNames.add(name);
	}

	public void removeName(String name)
	{
		allNames.remove(name);
		if (name.equals(standardName))
		{
			standardName = null;
		}
		if (name.equals(displayName))
		{
			displayName = null;
		}
	}

}
