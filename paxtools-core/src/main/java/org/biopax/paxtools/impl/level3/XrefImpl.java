package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.annotations.Field;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public abstract class XrefImpl extends L3ElementImpl implements Xref
{

	private String db;
	private String dbVersion;
	private String idVersion;
	private String id;
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
		final Xref anXref = (Xref) other;

		return
			(db != null ?
				db.equals(anXref.getDb()) :
				anXref.getDb() == null)
				&&
				(id != null ?
					id.equals(anXref.getId()) :
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
		result = 29 * result + (id != null ? id.hashCode() : 0);
		return result;
	}

	@Basic
	@Field(name=BioPAXElementImpl.SEARCH_FIELD_XREF_DB)
    public String getDb()
	{
		return db;
	}

	public void setDb(String db)
	{
		this.db = db;
	}


	@Basic
    public String getDbVersion()
	{
		return dbVersion;
	}

	public void setDbVersion(String dbVersion)
	{
		this.dbVersion = dbVersion;
	}

    @Basic
	public String getIdVersion()
	{
		return idVersion;
	}

	public void setIdVersion(String idVersion)
	{
		this.idVersion = idVersion;
	}

    // Property id
    @Basic
    @Field(name=BioPAXElementImpl.SEARCH_FIELD_XREF_ID)
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}


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
