package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 */
public interface TemplateReaction extends RestrictedInteraction
{
	// Property Product

	public Set<PhysicalEntity> getProduct();

	void addProduct(PhysicalEntity product);

	void removeProduct(PhysicalEntity product);

	public void setProduct(Set<PhysicalEntity> product);


	// Property Initiation Region

	public Set<NucleicAcid> getInitiationRegion();

	void addInitiationRegion(NucleicAcid initiationRegion);

	void removeInitiationRegion(NucleicAcid initiationRegion);

	public void setInitiationRegion(Set<NucleicAcid> initiationRegion);


	// Property template

	public NucleicAcid getTemplate();

	public void setTemplate(NucleicAcid template);


	public TemplateDirectionType getTemplateDirection();

	public void setTemplateDirection(TemplateDirectionType templateD1nmirection);


}
