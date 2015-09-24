package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public abstract class NamedImpl extends XReferrableImpl implements Named
{

	private String standardName;
	private String displayName;
	private Set<String> allNames;

	public NamedImpl()
	{
		allNames = BPCollections.I.createSet();
		displayName = null;
		standardName = null;
	}

	public String getStandardName()
	{
		return standardName;
	}

	public void setStandardName(String name)
	{
		addName(standardName = name);
	}
	
	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		addName(this.displayName = displayName);
	}
	
	public Set<String> getName()
	{
		return allNames;
	}

	public void setName(Set<String> names)
	{
		allNames = names;

		//also include the display and standard names if they're set
		if(displayName!=null && !displayName.isEmpty())
			allNames.add(displayName);
		if(standardName!=null && !standardName.isEmpty())
			allNames.add(standardName);
	}
	
	public void addName(String name)
	{
		if (name != null && name.length() > 0)
			allNames.add(name);
	}

	public void removeName(String name)
	{
		if (name != null)
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

}
