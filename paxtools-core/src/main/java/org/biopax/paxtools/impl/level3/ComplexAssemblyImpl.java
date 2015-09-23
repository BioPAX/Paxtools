package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ComplexAssembly;


public class ComplexAssemblyImpl extends ConversionImpl
	implements ComplexAssembly
{
	public ComplexAssemblyImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------
	
    @Override
	public Class<? extends ComplexAssembly> getModelInterface()
	{
		return ComplexAssembly.class;
	}
}
