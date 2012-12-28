package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.XrefFieldBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
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
				db.equalsIgnoreCase(anXref.getDb()) :
				anXref.getDb() == null)
				&&
				(refId != null ?
					refId.equals(anXref.getId()) :
					anXref.getId() == null)
				&&
				(dbVersion != null ?
					dbVersion.equalsIgnoreCase(anXref.getDbVersion()) :
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

	
	@Field(name=FIELD_XREFDB, index=Index.UN_TOKENIZED, bridge = @FieldBridge(impl=XrefFieldBridge.class))
	@Boost(1.1f)
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

	//Important! - using "id" as the search field name was causing exceptions in the indexer
	// using UN_TOKENIZED without a custom bridge also caused search problems (no result in trivial cases, where there should be one)
    @Field(name=FIELD_XREFID, index=Index.UN_TOKENIZED, bridge = @FieldBridge(impl=XrefFieldBridge.class))
    @Boost(1.1f)
    @Column(name="id")
	public String getIdx()
	{
		return refId;
	}

	public void setIdx(String id)
	{
		this.refId = id;
	}
    
    @Transient //non-transient 'getId()' used to cause Hibernate/Search trouble; so getIdx() was created.
    public String getId()
	{
		return refId;
	}

	public void setId(String id)
	{
		this.refId = id;
	}


	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
