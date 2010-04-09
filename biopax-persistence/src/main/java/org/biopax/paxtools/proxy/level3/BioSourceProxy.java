/*
 * BioSourceProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;
import org.hibernate.annotations.CollectionOfElements;

/**
 * Proxy for bioSource
 */
@Entity(name="l3biosource")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class BioSourceProxy extends Level3ElementProxy implements BioSource {

// Named
	
	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getName() {
		return ((BioSource)object).getName();
	}
	
	public void addName(String NAME_TEXT) {
		((BioSource)object).addName(NAME_TEXT);
	}
	
	public void removeName(String NAME_TEXT) {
		((BioSource)object).removeName(NAME_TEXT);
	}
	
	public void setName(Set<String> newNAME) {
		((BioSource)object).setName(newNAME);
	}
	
	@Basic @Column(name="display_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getDisplayName() {
		return ((BioSource)object).getDisplayName();
	}
	
	public void setDisplayName(String newDISPLAY_NAME) {
		((BioSource)object).setDisplayName(newDISPLAY_NAME);
	}
	
	@Basic @Column(name="standard_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getStandardName() {
		return ((BioSource)object).getStandardName();
	}
	
	public void setStandardName(String newSTANDARD_NAME) {
		((BioSource)object).setStandardName(newSTANDARD_NAME);
	}
	
// bioSource
	
	// Property CELLTYPE
	
	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = CellVocabularyProxy.class)
	@JoinColumn(name="cell_type_x")
	public CellVocabulary getCellType() {
		return ((BioSource)object).getCellType();
	}
	
	public void setCellType(CellVocabulary CELLTYPE) {
		((BioSource)object).setCellType(CELLTYPE);
	}
	
	// Property TAXON-XREF
	
	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = UnificationXrefProxy.class)
	@JoinColumn(name="taxon_xref_x")
	public UnificationXref getTaxonXref() {
		return ((BioSource)object).getTaxonXref();
	}
	
	public void setTaxonXref(UnificationXref TAXON_XREF) {
		((BioSource)object).setTaxonXref(TAXON_XREF);
	}
	
	// Property TISSUE
	
	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = TissueVocabularyProxy.class)
	@JoinColumn(name="tissue_x")
	public TissueVocabulary getTissue() {
		return ((BioSource)object).getTissue();
	}
	
	public void setTissue(TissueVocabulary TISSUE) {
		((BioSource)object).setTissue(TISSUE);
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return BioSource.class;
	}
}

