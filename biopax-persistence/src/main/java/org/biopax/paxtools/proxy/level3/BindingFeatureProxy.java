/*
 * BindingFeatureProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for BindingFeature
 */
@Entity(name="l3bindingfeature")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class BindingFeatureProxy extends EntityFeatureProxy implements BindingFeature {
	public BindingFeatureProxy() {
	}

    // Property BOUND-TO

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = BindingFeatureProxy.class)
	@JoinColumn(name="bound_to_x")
    public BindingFeature getBindsTo() {
		return ((BindingFeature)object).getBindsTo();
    }

    public void setBindsTo(BindingFeature bindsTo) {
		((BindingFeature)object).setBindsTo(bindsTo);
    }

	//property intramolecular

	// 2009.05.07 Takeshi Yoneki
	@Basic @Column(name="intra_molecular_x")
	public Boolean getIntraMolecular() {
		return ((BindingFeature)object).getIntraMolecular();
	}

	public void setIntraMolecular(Boolean intramolecular) {
		((BindingFeature)object).setIntraMolecular(intramolecular);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return BindingFeature.class;
	}

}
