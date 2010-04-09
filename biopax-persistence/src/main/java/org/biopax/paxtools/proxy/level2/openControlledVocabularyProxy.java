/*
 * OpenControlledVocabularyProxy.java
 *
 * 2007.03.16 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Set;

import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for openControlledVocabulary
 */
@Entity(name="l2opencontrolledvocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class openControlledVocabularyProxy extends externalReferenceUtilityClassProxy implements openControlledVocabulary {
	public openControlledVocabularyProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return openControlledVocabulary.class;
	}

	public void addTERM(String TERM) {
		((openControlledVocabulary)object).addTERM(TERM);
	}

	@CollectionOfElements @Column(name="term_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_TERM, index=Index.TOKENIZED)
	public Set<String> getTERM() {
		return ((openControlledVocabulary)object).getTERM();
	}

	public void removeTERM(String TERM) {
		((openControlledVocabulary)object).removeTERM(TERM);
	}

	public void setTERM(Set<String> TERM) {
		((openControlledVocabulary)object).setTERM(TERM);
	}

// --------------------- XReferrable ---------------------

	public void addXREF(xref XREF) {
		((openControlledVocabulary)object).addXREF(XREF);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=xrefProxy.class)
	@JoinTable(name="l2opencontrolledvocab_xref")
	public Set<xref> getXREF() {
		return ((openControlledVocabulary)object).getXREF();
	}

	public void removeXREF(xref XREF) {
		((openControlledVocabulary)object).removeXREF(XREF);
	}

	public void setXREF(Set<xref> XREF) {
		((openControlledVocabulary)object).setXREF(XREF);
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

