package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;

import java.util.HashMap;
import java.util.Map;

public abstract class InteractionRuleL2Adaptor implements InteractionRuleL2
{
	public void inferInteractions(InteractionSet interactionSet, BioPAXElement entity, Model model)
	{
		if(entity instanceof physicalEntity)
		{
			inferInteractionsFromPE(interactionSet, ((physicalEntity) entity),model);
		}
	}

	@Override public void initOptions(Map options)
	{
		if(options==null)
		{
			options = new HashMap();
		}
		initOptionsNotNull(options);
	}

	protected void initOptionsNotNull(Map options)
	{
		//do nothing
	}
}