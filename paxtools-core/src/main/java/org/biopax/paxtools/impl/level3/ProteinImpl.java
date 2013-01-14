package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Protein;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= Protein.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

@FetchProfile(name = "query", fetchOverrides = {
	@FetchProfile.FetchOverride(entity = CatalysisImpl.class, association = "cofactor", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = EntityReferenceImpl.class, association = "memberEntityReference", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = ComplexImpl.class, association = "component", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = ConversionImpl.class, association = "right", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = ControlImpl.class, association = "controlled", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = InteractionImpl.class, association = "participant", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = ControlImpl.class, association = "pathwayController", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = ControlImpl.class, association = "peController", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = ConversionImpl.class, association = "left", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = PhysicalEntityImpl.class, association = "memberPhysicalEntity", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = TemplateReactionImpl.class, association = "product", mode = FetchMode.JOIN),
	// Reverse properties
	@FetchProfile.FetchOverride(entity = ProcessImpl.class, association = "controlledOf", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = PhysicalEntityImpl.class, association = "memberPhysicalEntityOf", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = PhysicalEntityImpl.class, association = "controllerOf", mode = FetchMode.JOIN),
	@FetchProfile.FetchOverride(entity = EntityImpl.class, association = "participantOf", mode = FetchMode.JOIN)
	})

public class ProteinImpl extends SimplePhysicalEntityImpl implements Protein
{
	public ProteinImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends Protein> getModelInterface()
	{
		return Protein.class;
	}

}
