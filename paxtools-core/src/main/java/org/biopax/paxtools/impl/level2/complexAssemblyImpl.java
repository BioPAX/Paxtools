package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.complexAssembly;

/**
 * User: root Date: Apr 13, 2006 Time: 2:22:39 PM_DOT
 */
class complexAssemblyImpl extends conversionImpl
	implements complexAssembly
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return complexAssembly.class;
	}
}
