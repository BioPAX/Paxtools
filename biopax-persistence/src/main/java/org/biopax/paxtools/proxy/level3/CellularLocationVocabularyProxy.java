/*
 * CellularLocationVocabularyProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for cellularLocationVocabulary
 */
@Entity(name="l3cellularlocationvocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class CellularLocationVocabularyProxy extends ControlledVocabularyProxy implements
	CellularLocationVocabulary, Serializable {
	public CellularLocationVocabularyProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return CellularLocationVocabulary.class;
	}
}
