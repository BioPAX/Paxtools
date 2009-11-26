/*
 * CovalentBindingFeatureProxy.java
 *
 * 2009.05.07 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Proxy for CovalentBindingFeature
 */
@Entity(name="l3covalentbindingfeature")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class CovalentBindingFeatureProxy extends BindingFeatureProxy implements CovalentBindingFeature, Serializable {
	public CovalentBindingFeatureProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return CovalentBindingFeature.class;
	}

	// ModificationFeature Property

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SequenceModificationVocabularyProxy.class)
	@JoinColumn(name = "modification_type_x")
	public SequenceModificationVocabulary getModificationType() {
		return ((ModificationFeature) object).getModificationType();
	}

	public void setModificationType(SequenceModificationVocabulary featureType) {
		((ModificationFeature) object).setModificationType(featureType);
	}
}
