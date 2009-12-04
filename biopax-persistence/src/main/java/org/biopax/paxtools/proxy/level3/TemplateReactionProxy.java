/*
 * TemplateReactionProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Proxy for TemplateReaction
 */
@Entity(name="l3templatereaction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class TemplateReactionProxy extends RestrictedInteractionAdapterProxy 
	implements TemplateReaction 
{
	public TemplateReactionProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return TemplateReaction.class;
	}

	// Property Product

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PhysicalEntityProxy.class)
	@JoinTable(name="l3tempreact_product")
	public Set<PhysicalEntity> getProduct() {
		return ((TemplateReaction)object).getProduct();
	}

	public void addProduct(PhysicalEntity product) {
		((TemplateReaction)object).addProduct(product);
	}

	public void removeProduct(PhysicalEntity product) {
		((TemplateReaction)object).removeProduct(product);
	}

	public void setProduct(Set<PhysicalEntity> product) {
		((TemplateReaction)object).setProduct(product);
	}

	// Property RegulatoryElement

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= SimplePhysicalEntityProxy.class)
	@JoinTable(name="l3tempreact_regulatory_element")
	public Set<NucleicAcid> getInitiationRegion() {
		return ((TemplateReaction)object).getInitiationRegion();
	}

	public void addInitiationRegion(NucleicAcid initiationRegion) {
		((TemplateReaction)object).addInitiationRegion(initiationRegion);
	}

	public void removeInitiationRegion(NucleicAcid initiationRegion) {
		((TemplateReaction)object).removeInitiationRegion(initiationRegion);
	}

	public void setInitiationRegion(Set<NucleicAcid> initiationRegion) {
		((TemplateReaction)object).setInitiationRegion(initiationRegion);
	}

	// Property template

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SimplePhysicalEntityProxy.class)
	@JoinColumn(name="template_x")
	public NucleicAcid getTemplate() {
		return ((TemplateReaction)object).getTemplate();
	}

	public void setTemplate(NucleicAcid template) {
		((TemplateReaction)object).setTemplate(template);
	}

    // Property direction

	@Basic @Enumerated @Column(name="direction_x")
	public TemplateDirectionType getTemplateDirection() {
		return ((TemplateReaction)object).getTemplateDirection();
	}

	public void setTemplateDirection(TemplateDirectionType templateDirectionType) {
		((TemplateReaction)object).setTemplateDirection(templateDirectionType);
	}
}
