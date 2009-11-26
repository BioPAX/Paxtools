package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface Xref extends UtilityClass
{
   // Property DB

    String getDb();

    void setDb(String db);


    // Property DB-VERSION

    String getDbVersion();

    void setDbVersion(String dbVersion);


    // Property ID

    String getId();


    void setId(String id);


    // Property ID-VERSION

    String getIdVersion();

    void setIdVersion(String idVersion);


	// Inverse of property XREF
	
	public Set<XReferrable> getXrefOf();

}
