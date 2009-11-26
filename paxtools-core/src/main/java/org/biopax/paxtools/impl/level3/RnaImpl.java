package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Rna;

class RnaImpl extends SimplePhysicalEntityImpl implements Rna
{

// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends Rna> getModelInterface()
	{
		return Rna.class;
	}
}
