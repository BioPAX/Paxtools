/*
 * CovalentBindingFeatureProxy.java
 *
 * 2009.05.07 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for CovalentBindingFeature
 */
@Entity(name="l3covalentbindingfeature")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class CovalentBindingFeatureProxy extends BindingFeatureProxy<CovalentBindingFeature> implements CovalentBindingFeature {

	// ModificationFeature Property

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SequenceModificationVocabularyProxy.class)
	@JoinColumn(name = "modification_type_x")
	public SequenceModificationVocabulary getModificationType() {
		return object.getModificationType();
	}

	public void setModificationType(SequenceModificationVocabulary featureType) {
		object.setModificationType(featureType);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return CovalentBindingFeature.class;
	}
}
