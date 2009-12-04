/*
 * XrefProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for xref
 */
@Entity(name="l3xref")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class XrefProxy extends Level3ElementProxy 
	implements Xref 
{
	public XrefProxy() {
		// not get object. because this object has not factory.
	}

   // Property DB

	@Basic @Column(name="db_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_XREF_DB, index=Index.TOKENIZED)
	public String getDb() {
		return ((Xref)object).getDb();
	}

	public void setDb(String DB) {
		((Xref)object).setDb(DB);
	}

    // Property DB-VERSION

	@Basic @Column(name="db_version_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getDbVersion() {
		return ((Xref)object).getDbVersion();
	}

	public void setDbVersion(String DB_VERSION) {
		((Xref)object).setDbVersion(DB_VERSION);
	}

    // Property ID

	@Basic @Column(name="id_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_XREF_ID, index=Index.TOKENIZED)
	public String getId() {
		return ((Xref)object).getId();
	}

	public void setId(String ID) {
		((Xref)object).setId(ID);
	}

    // Property ID-VERSION

	@Basic @Column(name="id_version_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getIdVersion() {
		return ((Xref)object).getIdVersion();
	}

	public void setIdVersion(String ID_VERSION) {
		((Xref)object).setIdVersion(ID_VERSION);
	}

	// Inverse of property XREF

	@Transient
	public Set<XReferrable> getXrefOf() {
		return ((Xref)object).getXrefOf();
	}

}

