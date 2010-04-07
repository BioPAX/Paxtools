/*
 * XrefProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.XReferrable;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for xref
 */
@Entity(name="l2xref")
public abstract class xrefProxy extends externalReferenceUtilityClassProxy implements xref {
	public xrefProxy() {
		// not get object. because this object has not factory.
	}

	@Basic @Column(name="db_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_XREF_DB, index=Index.TOKENIZED)
	public String getDB() {
		return ((xref)object).getDB();
	}

	@Basic @Column(name="db_version_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getDB_VERSION() {
		return ((xref)object).getDB_VERSION();
	}

	@Basic @Column(name="id_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_XREF_ID, index=Index.TOKENIZED)
	public String getID() {
		return ((xref)object).getID();
	}

	@Basic @Column(name="id_version_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getID_VERSION() {
		return ((xref)object).getID_VERSION();
	}

	@Transient
	public Set<XReferrable> isXREFof() {
		return ((xref)object).isXREFof();
	}

	public void setDB(String DB) {
		((xref)object).setDB(DB);
	}

	public void setDB_VERSION(String DB_VERSION) {
		((xref)object).setDB_VERSION(DB_VERSION);
	}

	public void setID(String ID) {
		((xref)object).setID(ID);
	}

	public void setID_VERSION(String ID_VERSION) {
		((xref)object).setID_VERSION(ID_VERSION);
	}

}

