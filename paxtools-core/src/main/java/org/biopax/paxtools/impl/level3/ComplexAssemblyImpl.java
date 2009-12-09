package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ComplexAssembly;

/**
 * User: root Date: Apr 13, 2006 Time: 2:22:39 PM_DOT
 */
class ComplexAssemblyImpl extends ConversionImpl
	implements ComplexAssembly
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends ComplexAssembly> getModelInterface()
	{
		return ComplexAssembly.class;
	}
}
