/*
 * ModificationFeatureProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.*;

/**
 * Proxy for covalentFeature
 */
@Entity(name = "l3modificationfeature")
@Indexed(index = BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ModificationFeatureProxy extends EntityFeatureProxy implements ModificationFeature {
	public ModificationFeatureProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return ModificationFeature.class;
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SequenceModificationVocabularyProxy.class)
	@JoinColumn(name = "modification_type_x")
	public SequenceModificationVocabulary getModificationType() {
		return ((ModificationFeature) object).getModificationType();
	}

	public void setModificationType(SequenceModificationVocabulary featureType) {
		((ModificationFeature) object).setModificationType(featureType);
	}
}
