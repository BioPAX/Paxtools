package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.INTERACTS_WITH;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.REACTS_WITH;

/**
 * @author Emek Demir
 * @author Ozgun Babur
 */
public class ParticipatesRule implements InteractionRuleL3
{
	
	private final Log log = LogFactory.getLog(ParticipatesRule.class);
	
	public void inferInteractions(Set<SimpleInteraction> interactionSet, Object entity, Model model,
		Map options)
	{
		inferInteractions(interactionSet, ((EntityReference) entity), model, options);
	}

	public void inferInteractions(Set<SimpleInteraction> interactionSet, EntityReference er,
		Model model,Map options)
	{
		boolean skipConversions = options.containsKey(REACTS_WITH) &&
			options.get(REACTS_WITH).equals(false);

		boolean skipInteractions = options.containsKey(INTERACTS_WITH) &&
			options.get(INTERACTS_WITH).equals(false);

		// There is nothing to find if we are skipping both rules
		if (skipConversions && skipInteractions)
		{
			return;
		}

		for (PhysicalEntity pe : er.getEntityReferenceOf())
		{
			processPhysicalEntity(interactionSet, er, skipConversions, skipInteractions, pe);
		}
	}

	private void processPhysicalEntity(Set<SimpleInteraction> interactionSet, EntityReference er,
		boolean skipConversions, boolean skipInteractions, PhysicalEntity pe)
	{
		for (Interaction interaction : pe.getParticipantOf())
		{
			BinaryInteractionType type;
			if ((interaction instanceof Control))
			{
				continue;
			}
			else if (interaction instanceof Conversion)
			{
				if (skipConversions)
				{
					continue;
				}
				type = REACTS_WITH;
			}
			else
			{
				if (skipInteractions)
				{
					continue;
				}
				type = INTERACTS_WITH;
			}

			for (Entity partic : interaction.getParticipant())
			{
				processParticipant(interactionSet, er, partic, type, interaction);
			}
		}
		for (Complex comp : pe.getComponentOf())
		{
			processPhysicalEntity(interactionSet, er, skipConversions, skipInteractions, comp);
		}
	}

	private void processParticipant(Set<SimpleInteraction> interactionSet,
		EntityReference er,
		Entity entity,
		BinaryInteractionType type,
		Interaction interaction)
	{
		if (entity instanceof SimplePhysicalEntity)
		{
			EntityReference er2 = ((SimplePhysicalEntity) entity).getEntityReference();
			if(er2 != null) {
				createInteraction(er, er2, interactionSet, type, interaction);
			} else {
				if(log.isWarnEnabled())
					log.warn("Skip processing the interaction of EntityReference " 
						+ er + " with entity " + entity + ", which has NULL entityReference");
			}
		}
		else if (entity instanceof Complex)
		{
			for (EntityReference er2 : ((Complex) entity).getMemberReferences())
			{
				createInteraction(er, er2, interactionSet, type, interaction);
			}
		}
	}

	private void createInteraction(EntityReference er1, EntityReference er2,
		Set<SimpleInteraction> set,
		BinaryInteractionType type, Interaction interaction)
	{
		if (er2 != null && er1 != null && !er2.equals(er1))
		{
			SimpleInteraction si = new SimpleInteraction(er1, er2, type);
			si.addMediator(interaction);
			set.add(si);
		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(REACTS_WITH, INTERACTS_WITH);
	}
}