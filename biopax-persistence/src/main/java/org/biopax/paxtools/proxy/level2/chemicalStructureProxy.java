/*
 * ChemicalStructureProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.chemicalStructure;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Proxy for chemicalStructure
 */
@Entity(name="l2chemicalstructure")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class chemicalStructureProxy extends utilityClassProxy implements chemicalStructure, Serializable {
	public chemicalStructureProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return chemicalStructure.class;
	}

	@Basic @Column(name="structure_data_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getSTRUCTURE_DATA() {
		return ((chemicalStructure)object).getSTRUCTURE_DATA();
	}

	@Basic @Column(name="structure_format_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getSTRUCTURE_FORMAT() {
		return ((chemicalStructure)object).getSTRUCTURE_FORMAT();
	}

	public void setSTRUCTURE_DATA(String STRUCTURE_DATA) {
		((chemicalStructure)object).setSTRUCTURE_DATA(STRUCTURE_DATA);
	}

	public void setSTRUCTURE_FORMAT(String STRUCTURE_FORMAT) {
		((chemicalStructure)object).setSTRUCTURE_FORMAT(STRUCTURE_FORMAT);
	}
}

