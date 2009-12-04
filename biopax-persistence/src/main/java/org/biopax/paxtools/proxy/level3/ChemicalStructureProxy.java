/*
 * ChemicalStructureProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for chemicalStructure
 */
@Entity(name="l3chemicalstructure")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ChemicalStructureProxy extends Level3ElementProxy implements
	ChemicalStructure {
	public ChemicalStructureProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return ChemicalStructure.class;
	}

	// Property STRUCTURE-DATA

	@Basic @Column(name="structure_data_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getStructureData() {
		return ((ChemicalStructure)object).getStructureData();
	}

	public void setStructureData(String STRUCTURE_DATA) {
		((ChemicalStructure)object).setStructureData(STRUCTURE_DATA);
	}

	// Property STRUCTURE-FORMAT

	@Basic @Column(name="structure_format_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getStructureFormat() {
		return ((ChemicalStructure)object).getStructureFormat();
	}

	public void setStructureFormat(String STRUCTURE_FORMAT) {
		((ChemicalStructure)object).setStructureFormat(STRUCTURE_FORMAT);
	}
}

