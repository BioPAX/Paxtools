package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.rna;

class rnaImpl extends SequenceEntityImpl implements rna
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return rna.class;
	}
}
