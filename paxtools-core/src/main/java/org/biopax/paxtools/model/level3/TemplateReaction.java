package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 */
public interface TemplateReaction extends Interaction {
	// Property Product

	public Set<PhysicalEntity> getProduct();

	void addProduct(PhysicalEntity product);

	void removeProduct(PhysicalEntity product);


	// Property template

	public NucleicAcid getTemplate();

	public void setTemplate(NucleicAcid template);


	public TemplateDirectionType getTemplateDirection();

	public void setTemplateDirection(TemplateDirectionType templateD1nmirection);


}
