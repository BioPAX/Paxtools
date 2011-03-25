package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.unificationXref;

/**
 * User: root Date: Apr 26, 2006 Time: 3:12:15 PM_DOT
 */
class unificationXrefImpl extends xrefImpl implements unificationXref
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return unificationXref.class;
	}
}
