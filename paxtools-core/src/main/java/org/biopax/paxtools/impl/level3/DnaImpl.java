package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;


public class DnaImpl extends NucleicAcidImpl implements Dna
{
	
	public DnaImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------

	@Override
	public void setEntityReference(EntityReference entityReference) {
		if(entityReference instanceof DnaReference || entityReference == null)
			super.setEntityReference(entityReference);
		else
			throw new IllegalBioPAXArgumentException("setEntityReference failed: "
					+ entityReference.getUri() + " is not a DnaReference.");
	}

// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends Dna> getModelInterface()
	{
		return Dna.class;
	}

}
