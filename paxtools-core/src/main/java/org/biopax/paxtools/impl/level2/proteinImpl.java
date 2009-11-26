package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.BioPAXElement;

class proteinImpl extends SequenceEntityImpl implements protein
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return protein.class;
	}
}
