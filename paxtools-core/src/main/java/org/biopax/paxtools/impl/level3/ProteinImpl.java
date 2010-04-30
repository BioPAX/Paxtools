package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Protein;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class ProteinImpl extends SimplePhysicalEntityImpl implements Protein
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends Protein> getModelInterface()
	{
		return Protein.class;
	}

}
