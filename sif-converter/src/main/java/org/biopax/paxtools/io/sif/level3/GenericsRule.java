package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.GENERIC_OF;

/**
 */
public class GenericsRule extends InteractionRuleL3Adaptor
{

	private static final List<BinaryInteractionType> binaryInteractionTypes =  Arrays.asList(GENERIC_OF);

	public void inferInteractions(Set<SimpleInteraction> interactionSet, EntityReference entRef,
	                                        Model model,
	                                        Map options)
	{
		for (EntityReference member : entRef.getMemberEntityReference())
		{
			interactionSet.add(new SimpleInteraction(entRef, member, BinaryInteractionType.GENERIC_OF));
		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}
