package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.XReferrable;
import org.biopax.paxtools.model.level2.xref;

import java.util.HashSet;
import java.util.Set;

/**
 */
abstract class xrefImpl extends BioPAXLevel2ElementImpl implements xref
{
// ------------------------------ FIELDS ------------------------------

	private String DB;
	private String DB_VERSION;
	private String ID_VERSION;
	private String ID;
	private Set<XReferrable> XREFof;

// --------------------------- CONSTRUCTORS ---------------------------

// --------------------- ACCESORS and MUTATORS---------------------


	public xrefImpl()
	{
		this.XREFof = new HashSet<XReferrable>();
	}

// ------------------------ CANONICAL METHODS ------------------------

	public int equivalenceCode()
	{
		int result = 29 + (DB != null ? DB.hashCode() : 0);
		result = 29 * result + (DB_VERSION != null ? DB_VERSION.hashCode() : 0);
		result = 29 * result + (ID_VERSION != null ? ID_VERSION.hashCode() : 0);
		result = 29 * result + (ID != null ? ID.hashCode() : 0);
		return result;
	}

	public String toString()
	{
		return DB + ":" + ID;
	}

	// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	protected boolean semanticallyEquivalent(BioPAXElement other)
	{
		final xref anXref = (xref) other;

		return
			(DB != null ?
				DB.equalsIgnoreCase(anXref.getDB()) :
				anXref.getDB() == null)
				&&
				(ID != null ?
					ID.equalsIgnoreCase(anXref.getID()) :
					anXref.getID() == null)
				&&
				(DB_VERSION != null ?
					DB_VERSION.equalsIgnoreCase(anXref.getDB_VERSION()) :
					anXref.getDB_VERSION() == null)
				&&
				(ID_VERSION != null ?
					ID_VERSION.equalsIgnoreCase(anXref.getID_VERSION()) :
					anXref.getID_VERSION() != null);
	}

// --------------------- Interface xref ---------------------

	public String getDB()
	{
		return DB;
	}

	public void setDB(String DB)
	{
		this.DB = DB;
	}

	public String getDB_VERSION()
	{
		return DB_VERSION;
	}

	public void setDB_VERSION(String DB_VERSION)
	{
		this.DB_VERSION = DB_VERSION;
	}

	public String getID_VERSION()
	{
		return ID_VERSION;
	}

	public void setID_VERSION(String ID_VERSION)
	{
		this.ID_VERSION = ID_VERSION;
	}

	public String getID()
	{
		return ID;
	}

	public void setID(String ID)
	{
		this.ID = ID;
	}

	public Set<XReferrable> isXREFof()
	{
		return XREFof;
	}
}
