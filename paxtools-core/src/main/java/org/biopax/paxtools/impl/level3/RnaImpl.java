package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Rna;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class RnaImpl extends NucleicAcidImpl  implements Rna
{

// --------------------- Interface BioPAXElement ---------------------

	@Transient
    public Class<? extends Rna> getModelInterface()
	{
		return Rna.class;
	}
}
