package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Dna;


public class DnaImpl extends NucleicAcidImpl implements Dna
{
	
	public DnaImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends Dna> getModelInterface()
	{
		return Dna.class;
	}

}
