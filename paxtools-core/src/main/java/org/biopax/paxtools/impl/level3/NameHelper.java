package org.biopax.paxtools.impl.level3;

import java.io.Serializable;
import java.util.*;

/**
 * @author Rex Dwyer
 */
class NameHelper implements Serializable
{

	/**
	 * Fields **
	 */
	private String standardName = null;
	private String displayName = null;
	private Set<String> allNames = new HashSet<String>();

	/**
	 * Getters **
	 */
	public String getStandardName()
	{
		return standardName;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public Set<String> getName()
	{
		return allNames;
	}

	/**
	 * Setters **
	 */
	public void setStandardName(String name)
	{
		addName(standardName = name);
	}

	public void setDisplayName(String name)
	{
		addName(displayName = name);
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

	/**
	 * Add/Remove Names **
	 */
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
