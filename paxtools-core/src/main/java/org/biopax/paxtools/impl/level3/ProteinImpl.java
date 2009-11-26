package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Protein;

class ProteinImpl extends SimplePhysicalEntityImpl implements Protein
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends Protein> getModelInterface()
	{
		return Protein.class;
	}

}
