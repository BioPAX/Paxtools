package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.relationshipXref;

/**
 */
class relationshipXrefImpl extends xrefImpl implements relationshipXref
{
// ------------------------------ FIELDS ------------------------------

	private String RELATIONSHIP_TYPE;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return relationshipXref.class;
	}

// --------------------- Interface relationshipXref ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public String getRELATIONSHIP_TYPE()
	{
		return RELATIONSHIP_TYPE;
	}

	public void setRELATIONSHIP_TYPE(String RELATIONSHIP_TYPE)
	{
		this.RELATIONSHIP_TYPE = RELATIONSHIP_TYPE;
	}
}
