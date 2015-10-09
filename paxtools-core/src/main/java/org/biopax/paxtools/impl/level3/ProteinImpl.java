package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;


public class ProteinImpl extends SimplePhysicalEntityImpl implements Protein
{
	public ProteinImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------

	@Override
	public void setEntityReference(EntityReference entityReference) {
		if(entityReference instanceof ProteinReference || entityReference == null)
			super.setEntityReference(entityReference);
		else
			throw new IllegalBioPAXArgumentException("setEntityReference failed: "
					+ entityReference.getUri() + " is not a ProteinReference.");
	}


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends Protein> getModelInterface()
	{
		return Protein.class;
	}

}
