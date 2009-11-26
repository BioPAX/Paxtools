/*
 * DataSourceProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for dataSource
 */
@Entity(name="l2datasource")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class dataSourceProxy extends externalReferenceUtilityClassProxy implements dataSource, Serializable {
	public dataSourceProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return dataSource.class;
	}

	public void addXREF(xref XREF) {
		((dataSource)object).addXREF(XREF);
	}
	
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=xrefProxy.class)
	@JoinTable(name="l2datasource_xref")
	public Set<xref> getXREF() {
		return ((dataSource)object).getXREF();
	}

	public void removeXREF(xref XREF) {
		((dataSource)object).removeXREF(XREF);
	}

	public void setXREF(Set<xref> XREF) {
		((dataSource)object).setXREF(XREF);
	}

//	@Transient
	public Set<unificationXref> findCommonUnifications(XReferrable that)
	{
		return ((XReferrable) object).findCommonUnifications(that);
	}

//	@Transient
	public Set<relationshipXref> findCommonRelationships(XReferrable that)
	{
		return ((XReferrable) object).findCommonRelationships(that);
	}

//	@Transient
	public Set<publicationXref> findCommonPublications(XReferrable that)
	{
		return ((XReferrable) object).findCommonPublications(that);
	}


	public void addNAME(String NAME) {
		((dataSource)object).addNAME(NAME);
	}

	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public Set<String> getNAME() {
		return ((dataSource)object).getNAME();
	}

	public void removeNAME(String NAME) {
		((dataSource)object).removeNAME(NAME);
	}

	public void setNAME(Set<String> NAME) {
		((dataSource)object).setNAME(NAME);
	}
}
