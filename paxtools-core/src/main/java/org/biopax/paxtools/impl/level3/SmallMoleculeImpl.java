package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class SmallMoleculeImpl extends SimplePhysicalEntityImpl
		implements SmallMolecule
{
	
	@Override @Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return SmallMolecule.class;
	}
	
}
