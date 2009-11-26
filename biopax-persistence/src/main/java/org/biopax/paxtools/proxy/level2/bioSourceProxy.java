/*
 * BioSourceProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Proxy for bioSource
 */
@Entity(name="l2biosource")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class bioSourceProxy extends externalReferenceUtilityClassProxy implements bioSource, Serializable {
	public bioSourceProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return bioSource.class;
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=openControlledVocabularyProxy.class)
	@JoinColumn(name="celltype_x")
	public openControlledVocabulary getCELLTYPE() {
		return ((bioSource)object).getCELLTYPE();
	}

	@Basic @Column(name="name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getNAME() {
		return ((bioSource)object).getNAME();
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=unificationXrefProxy.class)
	@JoinColumn(name="taxon_xref_x")
	public unificationXref getTAXON_XREF() {
		return ((bioSource)object).getTAXON_XREF();
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=openControlledVocabularyProxy.class)
	@JoinColumn(name="tissue_x")
	public openControlledVocabulary getTISSUE() {
		return ((bioSource)object).getTISSUE();
	}

	public void setCELLTYPE(openControlledVocabulary CELLTYPE) {
		((bioSource)object).setCELLTYPE(CELLTYPE);
	}

	public void setNAME(String NAME) {
		((bioSource)object).setNAME(NAME);
	}

	public void setTAXON_XREF(unificationXref TAXON_XREF) {
		((bioSource)object).setTAXON_XREF(TAXON_XREF);
	}

	public void setTISSUE(openControlledVocabulary TISSUE) {
		((bioSource)object).setTISSUE(TISSUE);
	}

}

