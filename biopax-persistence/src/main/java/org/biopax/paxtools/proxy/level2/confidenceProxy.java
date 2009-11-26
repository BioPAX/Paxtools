/*
 * confidenceProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Proxy for confidence
 */
@Entity(name="l2confidence")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class confidenceProxy extends utilityClassProxy implements confidence, Serializable {
	public confidenceProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return confidence.class;
	}

	@Basic @Column(name="confidence_value_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getCONFIDENCE_VALUE() {
		return ((confidence)object).getCONFIDENCE_VALUE();
	}

	public void setCONFIDENCE_VALUE(String CONFIDENCE_VALUE) {
		((confidence)object).setCONFIDENCE_VALUE(CONFIDENCE_VALUE);
	}

// --------------------- XReferrable ---------------------

	public void addXREF(xref XREF) {
		((confidence)object).addXREF(XREF);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=xrefProxy.class)
	@JoinTable(name="l2confidence_xref")
	public Set<xref> getXREF() {
		return ((confidence)object).getXREF();
	}

	public void removeXREF(xref XREF) {
		((confidence)object).removeXREF(XREF);
	}

	public void setXREF(Set<xref> XREF) {
		((confidence)object).setXREF(XREF);
	}

	@Transient
	public Set<unificationXref> findCommonUnifications(XReferrable that)
	{
		return ((XReferrable) object).findCommonUnifications(that);
	}

	@Transient
	public Set<relationshipXref> findCommonRelationships(XReferrable that)
	{
		return ((XReferrable) object).findCommonRelationships(that);
	}

	@Transient
	public Set<publicationXref> findCommonPublications(XReferrable that)
	{
		return ((XReferrable) object).findCommonPublications(that);
	}

}

