package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
 @Proxy(proxyClass= Modulation.class)
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ModulationImpl extends ControlImpl implements Modulation
{
	
	public ModulationImpl() {
	}

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
