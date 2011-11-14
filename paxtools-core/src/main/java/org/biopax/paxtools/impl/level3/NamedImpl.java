package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Transient;

import java.util.HashSet;
import java.util.Set;

/**
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class NamedImpl extends XReferrableImpl
{

	private String standardName;
	private String displayName;
	private Set<String> allNames;

	public NamedImpl()
	{
		allNames = new HashSet<String>();
		displayName = null;
		standardName = null;
	}

	
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_NAME, index = Index.TOKENIZED)
	@Boost(2.0f)
	@Column(columnDefinition="LONGTEXT")
	protected String getStandardNameX()
	{
		return standardName;
	}
	protected void setStandardNameX(String name)
	{
		standardName = name;
	}

	@Transient
	public String getStandardName()
	{
		return standardName;
	}

	public void setStandardName(String name)
	{
		addName(standardName = name);
	}
	
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_NAME, index = Index.TOKENIZED)
	@Boost(2.0f)
	@Column(columnDefinition="LONGTEXT")
	protected String getDisplayNameX()
	{
		return displayName;
	}
	protected void setDisplayNameX(String displayName)
	{
		this.displayName = displayName;
	}
	
	@Transient
	public String getDisplayName()
	{
		return displayName;
	}
	
	public void setDisplayName(String displayName)
	{
		addName(this.displayName = displayName);
	}
	
	
	@ElementCollection
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_NAME, index = Index.TOKENIZED)
	@FieldBridge(impl = SetStringBridge.class)
	@Boost(1.0f)
	@Column(columnDefinition="LONGTEXT")
	protected Set<String> getNameX()
	{
		return allNames;
	}
	protected void setNameX(Set<String> names)
	{
		allNames = names;
	}
	
	
	@Transient
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
