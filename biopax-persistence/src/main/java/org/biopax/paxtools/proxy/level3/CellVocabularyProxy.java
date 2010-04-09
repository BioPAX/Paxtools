/*
 * CellVocabularyProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import javax.persistence.Transient;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

/**
 * Proxy for cellVocabulary
 */
@javax.persistence.Entity(name="l3cellvocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class CellVocabularyProxy extends ControlledVocabularyProxy<CellVocabulary>
	implements CellVocabulary 
{
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return CellVocabulary.class;
	}
}
