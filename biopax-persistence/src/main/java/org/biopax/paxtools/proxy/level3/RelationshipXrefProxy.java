/*
 * RelationshipXrefProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for relationshipXref
 */
@Entity(name="l3relationshipxref")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RelationshipXrefProxy extends XrefProxy implements RelationshipXref, Serializable {
	public RelationshipXrefProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return RelationshipXref.class;
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = RelationshipTypeVocabularyProxy.class)
	@JoinColumn(name="relationship_type_x")
	public RelationshipTypeVocabulary getRelationshipType() {
		return ((RelationshipXref)object).getRelationshipType();
	}

	public void setRelationshipType(RelationshipTypeVocabulary RELATIONSHIP_TYPE) {
		((RelationshipXref)object).setRelationshipType(RELATIONSHIP_TYPE);
	}
}

