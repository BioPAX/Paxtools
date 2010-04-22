/*
/ * XrefProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Proxy for xref
 */
@Entity(name="l3xref")
public abstract class XrefProxy<T extends Xref> extends Level3ElementProxy<T>
	implements Xref 
{
   // Property DB

	@Basic @Column(name="db_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_XREF_DB, index=Index.TOKENIZED)
	public String getDb() {
		return ((Xref)object).getDb();
	}

	public void setDb(String DB) {
		object.setDb(DB);
	}

    // Property DB-VERSION

	@Basic @Column(name="db_version_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getDbVersion() {
		return object.getDbVersion();
	}

	public void setDbVersion(String DB_VERSION) {
		object.setDbVersion(DB_VERSION);
	}

    // Property ID

	@Basic @Column(name="id_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_XREF_ID, index=Index.TOKENIZED)
	public String getId() {
		return object.getId();
	}

	public void setId(String ID) {
		object.setId(ID);
	}

    // Property ID-VERSION

	@Basic @Column(name="id_version_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getIdVersion() {
		return object.getIdVersion();
	}

	public void setIdVersion(String ID_VERSION) {
		object.setIdVersion(ID_VERSION);
	}

	// Inverse of property XREF

	@ManyToMany(targetEntity = XReferrableProxy.class, mappedBy="xref")
	public Set<XReferrable> getXrefOf() {
		return object.getXrefOf();
	}

	private void setXrefOf(Set<XReferrable> set)
	{
 	  updateSet(set, object.getXrefOf());
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface()
	{
		return Xref.class;
	}
}

