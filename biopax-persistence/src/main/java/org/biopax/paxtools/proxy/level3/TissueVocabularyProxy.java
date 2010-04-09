/*
 * TissueVocabularyProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for tissueVocabulary
 */
@Entity(name="l3tissuevocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class TissueVocabularyProxy extends ControlledVocabularyProxy<TissueVocabulary>
	implements	TissueVocabulary 
{
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return TissueVocabulary.class;
	}
}
