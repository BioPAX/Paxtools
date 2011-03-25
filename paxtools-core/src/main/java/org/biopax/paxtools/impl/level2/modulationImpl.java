package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

/**
 */
class modulationImpl extends controlImpl implements modulation
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return modulation.class;
	}

// -------------------------- OTHER METHODS --------------------------

// --------------------- ACCESORS and MUTATORS---------------------
	protected void checkCONTROLLED(entity CONTROLLED)
	{
		//this has to be a conversion
		if (!(CONTROLLED instanceof catalysis))
		{
			throw new IllegalBioPAXArgumentException(
				"Controlled can only be a catalysis");
		}
	}


	protected boolean checkCONTROLLED(process controlled)
	{
		return controlled instanceof catalysis;
	}
}
