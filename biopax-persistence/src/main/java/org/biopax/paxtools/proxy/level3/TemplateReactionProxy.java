/*
 * TemplateReactionProxy.java
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

import java.util.Set;

/**
 * Proxy for TemplateReaction
 */
@Entity(name="l3templatereaction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class TemplateReactionProxy extends InteractionProxy<TemplateReaction> 
	implements TemplateReaction 
{

	// Property Product

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PhysicalEntityProxy.class)
	@JoinTable(name="l3tempreact_product")
	public Set<PhysicalEntity> getProduct() {
		return object.getProduct();
	}

	public void addProduct(PhysicalEntity product) {
		object.addProduct(product);
	}

	public void removeProduct(PhysicalEntity product) {
		object.removeProduct(product);
	}

	public void setProduct(Set<PhysicalEntity> product) {
		object.setProduct(product);
	}

	// Property RegulatoryElement

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= SimplePhysicalEntityProxy.class)
	@JoinTable(name="l3tempreact_regulatory_element")
	public Set<NucleicAcid> getInitiationRegion() {
		return object.getInitiationRegion();
	}

	public void addInitiationRegion(NucleicAcid initiationRegion) {
		object.addInitiationRegion(initiationRegion);
	}

	public void removeInitiationRegion(NucleicAcid initiationRegion) {
		object.removeInitiationRegion(initiationRegion);
	}

	public void setInitiationRegion(Set<NucleicAcid> initiationRegion) {
		object.setInitiationRegion(initiationRegion);
	}

	// Property template

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SimplePhysicalEntityProxy.class)
	@JoinColumn(name="template_x")
	public NucleicAcid getTemplate() {
		return object.getTemplate();
	}

	public void setTemplate(NucleicAcid template) {
		object.setTemplate(template);
	}

    // Property direction

	@Basic @Enumerated @Column(name="direction_x")
	public TemplateDirectionType getTemplateDirection() {
		return object.getTemplateDirection();
	}

	public void setTemplateDirection(TemplateDirectionType templateDirectionType) {
		object.setTemplateDirection(templateDirectionType);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return TemplateReaction.class;
	}
}
