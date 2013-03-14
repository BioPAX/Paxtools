package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for level 2 rules.
 */
public abstract class InteractionRuleL2Adaptor implements InteractionRuleL2
{
	/**
	 * Uses physicalEntity as source of the interaction.
	 * @param interactionSet to populate
	 * @param entity this must be a physicalEntity for L2, and PhysicalEntity for L3
	 * @param model BioPAX model
	 */
	public void inferInteractions(InteractionSet interactionSet, BioPAXElement entity, Model model)
	{
		if(entity instanceof physicalEntity)
		{
			inferInteractionsFromPE(interactionSet, ((physicalEntity) entity),model);
		}
	}

	/**
	 * Initializes options.
	 * @param options options map
	 */
	@Override public void initOptions(Map options)
	{
		if(options==null)
		{
			options = new HashMap();
		}
		initOptionsNotNull(options);
	}

	/**
	 * Does nothing here. Overridden in children. options is guaranteed to be not null.
	 * @param options options map
	 */
	protected void initOptionsNotNull(Map options)
	{
		//do nothing
	}
}