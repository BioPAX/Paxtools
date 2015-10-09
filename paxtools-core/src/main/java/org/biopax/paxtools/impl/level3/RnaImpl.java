package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaReference;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;


public class RnaImpl extends NucleicAcidImpl  implements Rna
{

	public RnaImpl() {
	}

	@Override
	public void setEntityReference(EntityReference entityReference) {
		if(entityReference instanceof RnaReference || entityReference == null)
			super.setEntityReference(entityReference);
		else
			throw new IllegalBioPAXArgumentException("setEntityReference failed: "
					+ entityReference.getUri() + " is not a RnaReference.");
	}

// --------------------- Interface BioPAXElement ---------------------

    public Class<? extends Rna> getModelInterface()
	{
		return Rna.class;
	}
}
