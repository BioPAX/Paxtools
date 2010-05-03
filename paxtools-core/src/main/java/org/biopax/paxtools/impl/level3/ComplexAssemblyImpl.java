package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
class ComplexAssemblyImpl extends ConversionImpl
	implements ComplexAssembly
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override   @Transient
	public Class<? extends ComplexAssembly> getModelInterface()
	{
		return ComplexAssembly.class;
	}
}
