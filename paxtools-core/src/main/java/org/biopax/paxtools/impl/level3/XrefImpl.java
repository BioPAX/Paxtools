package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
 @Proxy(proxyClass= Xref.class)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class XrefImpl extends L3ElementImpl implements Xref
{

	private String db;
	private String dbVersion;
	private String idVersion;
	private String refId;
	private Set<XReferrable> xrefOf;

	/**
	 * Constructor.
	 */
	public XrefImpl()
	{
		this.xrefOf = new HashSet<XReferrable>();
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement other)
	{
		if(!(other instanceof Xref)) return false;
		
		final Xref anXref = (Xref) other;

		return
			(db != null ?
				db.equals(anXref.getDb()) :
				anXref.getDb() == null)
				&&
				(refId != null ?
					refId.equals(anXref.getId()) :
					anXref.getId() == null)
				&&
				(dbVersion != null ?
					dbVersion.equals(anXref.getDbVersion()) :
					anXref.getDbVersion() == null)
				&&
				(idVersion != null ?
					idVersion.equals(anXref.getIdVersion()) :
					anXref.getIdVersion() == null);
	}

    @Override
	public int equivalenceCode()
	{
		int result = 29 + (db != null ? db.hashCode() : 0);
		result = 29 * result + (dbVersion != null ? dbVersion.hashCode() : 0);
		result = 29 * result + (idVersion != null ? idVersion.hashCode() : 0);
		result = 29 * result + (refId != null ? refId.hashCode() : 0);
		return result;
	}

	
	@Field(name=BioPAXElementImpl.SEARCH_FIELD_XREF_DB, index=Index.TOKENIZED)
    public String getDb()
	{
		return db;
	}

	public void setDb(String db)
	{
		this.db = db;
	}

    public String getDbVersion()
	{
		return dbVersion;
	}

	public void setDbVersion(String dbVersion)
	{
		this.dbVersion = dbVersion;
	}

	public String getIdVersion()
	{
		return idVersion;
	}

	public void setIdVersion(String idVersion)
	{
		this.idVersion = idVersion;
	}

    @Field(name=BioPAXElementImpl.SEARCH_FIELD_XREF_ID, index=Index.TOKENIZED)
    @Column(name="id")
	public String getIdx()
	{
		return refId;
	}

	public void setIdx(String id)
	{
		this.refId = id;
	}
    
    @Transient
    public String getId()
	{
		return refId;
	}

	public void setId(String id)
	{
		this.refId = id;
	}


	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ContainedIn
	@ManyToMany(targetEntity = XReferrableImpl.class, mappedBy = "xref")
	public Set<XReferrable> getXrefOf()
	{
		return xrefOf;
	}

	protected void setXrefOf(Set<XReferrable> xrefOf)
	{
		this.xrefOf = xrefOf;
	}

	@Override
	public String toString() {
		return getDb() + 
		((getDbVersion()==null)? "" : "." + getDbVersion()) 
		+ ":" + getId() + 
		((getIdVersion()==null)? "" : "." + getIdVersion());
	}
	
}
