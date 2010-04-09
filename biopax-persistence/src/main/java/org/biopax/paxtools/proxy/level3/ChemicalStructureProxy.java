/*
 * ChemicalStructureProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Transient;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

/**
 * Proxy for chemicalStructure
 */
@javax.persistence.Entity(name="l3chemicalstructure")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ChemicalStructureProxy extends Level3ElementProxy<ChemicalStructure> implements
	ChemicalStructure {
	// Property STRUCTURE-DATA

	@Basic @Column(name="structure_data_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getStructureData() {
		return object.getStructureData();
	}

	public void setStructureData(String STRUCTURE_DATA) {
		object.setStructureData(STRUCTURE_DATA);
	}

	// Property STRUCTURE-FORMAT

	@Basic @Column(name="structure_format_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getStructureFormat() {
		return object.getStructureFormat();
	}

	public void setStructureFormat(String STRUCTURE_FORMAT) {
		object.setStructureFormat(STRUCTURE_FORMAT);
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return ChemicalStructure.class;
	}
}

