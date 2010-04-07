/*
 * RelationshipXrefProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.*;

/**
 * Proxy for relationshipXref
 */
@Entity(name="l2relationshipxref")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class relationshipXrefProxy extends xrefProxy implements relationshipXref {
	public relationshipXrefProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return relationshipXref.class;
	}

	@Basic @Column(name="relationship_type_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getRELATIONSHIP_TYPE() {
		return ((relationshipXref)object).getRELATIONSHIP_TYPE();
	}

	public void setRELATIONSHIP_TYPE(String RELATIONSHIP_TYPE) {
		((relationshipXref)object).setRELATIONSHIP_TYPE(RELATIONSHIP_TYPE);
	}
}

