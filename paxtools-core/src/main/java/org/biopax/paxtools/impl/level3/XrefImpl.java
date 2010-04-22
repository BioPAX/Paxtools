package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;

import java.util.HashSet;
import java.util.Set;

abstract class XrefImpl extends L3ElementImpl implements Xref
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

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

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

	//
	// Xref interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

   // Property db

	public String getDb()
	{
		return db;
	}

	public void setDb(String db)
	{
		this.db = db;
	}

    // Property db-VERSION

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

    // Property id

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	// Inverse of property Xref

	public Set<XReferrable> getXrefOf()
	{
		return xrefOf;
	}
	
	@Override
	public String toString() {
		return getDb() + 
		((getDbVersion()==null)? "" : "." + getDbVersion()) 
		+ ":" + getId() + 
		((getIdVersion()==null)? "" : "." + getIdVersion());
	}
	
}
