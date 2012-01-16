package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 */
@Entity
@Proxy(proxyClass= Named.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class NamedImpl extends XReferrableImpl implements Named
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

	
	@Field(name = "name", index = Index.TOKENIZED)
	@Boost(2.5f)
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
	
	@Field(name = "name", index = Index.TOKENIZED)
	@Boost(2f)
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
	
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @ElementCollection
	@JoinTable(name="name")
	@Field(name = "name", index = Index.TOKENIZED)
	@FieldBridge(impl = SetStringBridge.class)
	@Boost(2.0f)
	@Column(columnDefinition="LONGTEXT")
	public Set<String> getName()
	{
		return allNames;
	}

	public void setName(Set<String> names)
	{
		allNames = names;
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
