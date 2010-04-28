package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.Process;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class ModulationImpl extends ControlImpl implements Modulation
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends Modulation> getModelInterface()
	{
		return Modulation.class;
	}

// -------------------------- OTHER METHODS --------------------------



	protected boolean checkControlled(Process controlled)
	{
		return controlled instanceof Catalysis;
	}
}
