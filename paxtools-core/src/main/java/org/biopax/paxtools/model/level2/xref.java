package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface xref extends externalReferenceUtilityClass
{
// -------------------------- OTHER METHODS --------------------------

// --------------------- ACCESORS and MUTATORS---------------------
	String getDB();

	String getDB_VERSION();

	String getID();

	String getID_VERSION();

	Set<XReferrable> isXREFof();

	void setDB(String DB);

	void setDB_VERSION(String DB_VERSION);

	void setID(String ID);

	void setID_VERSION(String ID_VERSION);
}