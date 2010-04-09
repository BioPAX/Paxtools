/*
 * BindingFeatureProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
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
 * Proxy for BindingFeature
 */
@Entity(name="l3bindingfeature")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class BindingFeatureProxy<T extends BindingFeature> extends EntityFeatureProxy<T> implements BindingFeature {

    // Property BOUND-TO

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = BindingFeatureProxy.class)
	@JoinColumn(name="bound_to_x")
    public BindingFeature getBindsTo() {
		return object.getBindsTo();
    }

    public void setBindsTo(BindingFeature bindsTo) {
		object.setBindsTo(bindsTo);
    }

	//property intramolecular

	// 2009.05.07 Takeshi Yoneki
	@Basic @Column(name="intra_molecular_x")
	public Boolean getIntraMolecular() {
		return object.getIntraMolecular();
	}

	public void setIntraMolecular(Boolean intramolecular) {
		object.setIntraMolecular(intramolecular);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return BindingFeature.class;
	}

}
