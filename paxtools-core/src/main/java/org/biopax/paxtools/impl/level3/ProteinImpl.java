package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Protein;


public class ProteinImpl extends SimplePhysicalEntityImpl implements Protein
{
	public ProteinImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends Protein> getModelInterface()
	{
		return Protein.class;
	}

}
