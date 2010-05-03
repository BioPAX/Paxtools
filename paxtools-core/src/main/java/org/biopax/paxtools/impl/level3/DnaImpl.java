package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Dna;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;


@Entity
@Indexed
class DnaImpl extends NucleicAcidImpl implements Dna
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends Dna> getModelInterface()
	{
		return Dna.class;
	}

}
