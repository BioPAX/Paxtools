package org.biopax.paxtools.model.level3;

import java.util.Set;

public interface TemplateReaction extends Interaction {
	// Property Product

	Set<PhysicalEntity> getProduct();

	void addProduct(PhysicalEntity product);

	void removeProduct(PhysicalEntity product);

	// Property template

	NucleicAcid getTemplate();

	void setTemplate(NucleicAcid template);

	TemplateDirectionType getTemplateDirection();

	void setTemplateDirection(TemplateDirectionType templateD1nmirection);

}
