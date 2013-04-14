package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Protein;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= Protein.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProteinImpl extends SimplePhysicalEntityImpl implements Protein
{
	public ProteinImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends Protein> getModelInterface()
	{
		return Protein.class;
	}

}
