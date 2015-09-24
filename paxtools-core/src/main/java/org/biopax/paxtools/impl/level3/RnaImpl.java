package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Rna;


public class RnaImpl extends NucleicAcidImpl  implements Rna
{

	public RnaImpl() {
	}
// --------------------- Interface BioPAXElement ---------------------

    public Class<? extends Rna> getModelInterface()
	{
		return Rna.class;
	}
}
