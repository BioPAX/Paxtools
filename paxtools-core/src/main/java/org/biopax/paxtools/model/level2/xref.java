package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface xref extends externalReferenceUtilityClass
{
// -------------------------- OTHER METHODS --------------------------

// --------------------- ACCESORS and MUTATORS---------------------
	public String getDB();

	public String getDB_VERSION();

	public String getID();


	public String getID_VERSION();

	public Set<XReferrable> isXREFof();

	public void setDB(String DB);

	public void setDB_VERSION(String DB_VERSION);

	public void setID(String ID);

	public void setID_VERSION(String ID_VERSION);
}