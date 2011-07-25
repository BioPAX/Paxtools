package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public abstract class InteractionRuleL3Adaptor implements InteractionRuleL3
{

	@Override
	public final void inferInteractions(Set<SimpleInteraction> interactionSet, Object entity, Model model, Map options)
	{
		if(entity instanceof EntityReference)
		{
			inferInteractions(interactionSet, ((EntityReference) entity),model,options);
		}
	}

}
