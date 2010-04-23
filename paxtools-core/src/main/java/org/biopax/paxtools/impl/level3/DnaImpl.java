package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Dna;

import javax.persistence.Entity;


@Entity
class DnaImpl extends SimplePhysicalEntityImpl implements Dna
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends Dna> getModelInterface()
	{
		return Dna.class;
	}

}
