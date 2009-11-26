package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;

/**
 */
class SmallMoleculeImpl extends SimplePhysicalEntityImpl
		implements SmallMolecule
{
	
	@Override
	public Class<? extends PhysicalEntity> getModelInterface() {
		return SmallMolecule.class;
	}
	
}
