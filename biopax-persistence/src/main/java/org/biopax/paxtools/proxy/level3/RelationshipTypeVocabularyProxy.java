/*
 * RelationshipTypeVocabularyProxy.java
 *
 * 2008.06.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for RelationshipTypeVocabulary
 */
@Entity(name="l3relationshiptypevocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RelationshipTypeVocabularyProxy extends ControlledVocabularyProxy<RelationshipTypeVocabulary> 
	implements RelationshipTypeVocabulary 
{
    
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return RelationshipTypeVocabulary.class;
	}
}
