package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.BioPAXElement;

/**
 */
class dnaImpl extends SequenceEntityImpl implements dna
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return dna.class;
	}
}
