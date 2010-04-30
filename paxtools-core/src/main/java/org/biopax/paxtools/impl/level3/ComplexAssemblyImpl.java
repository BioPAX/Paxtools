package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ComplexAssembly;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
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
