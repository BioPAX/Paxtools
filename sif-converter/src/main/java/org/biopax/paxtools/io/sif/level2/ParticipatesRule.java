package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.REACTS_WITH;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.INTERACTS_WITH;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: demir Date: Dec 28, 2007 Time: 5:52:06 PM
 */
public class ParticipatesRule implements InteractionRuleL2
{
	public void inferInteractions(Set<SimpleInteraction> interactionSet,
		Object entity,
		Model model, Map options)
	{
		inferInteractions(interactionSet, ((physicalEntity) entity), model, options);
	}

	public void inferInteractions(Set<SimpleInteraction> interactionSet,
	                              physicalEntity pe, Model model,
	                              Map options)
	{
		boolean skipConversions =
			options.containsKey(REACTS_WITH) &&
				options.get(REACTS_WITH).equals(false);

		boolean skipInteractions =
			options.containsKey(INTERACTS_WITH) &&
				options.get(INTERACTS_WITH).equals(false);

		// There is nothing to find if we are skipping both rules
		if (skipConversions && skipInteractions)
		{
			return;
		}

		Set<interaction> interactions = pe.getAllInteractions();
		for (interaction interaction : interactions)
		{
			BinaryInteractionType type;
			if ((interaction instanceof control))
			{
				return;
			}
			else if (interaction instanceof conversion)
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

			Set<InteractionParticipant> ips = interaction.getPARTICIPANTS();
			for (InteractionParticipant ip : ips)
			{
				processParticipant(interactionSet, pe, ip, type, interaction);
			}
		}
	}

	private void processParticipant(Set<SimpleInteraction> interactionSet,
                                    physicalEntity pe,
                                    InteractionParticipant ip,
                                    BinaryInteractionType type, interaction interaction)
	{
		physicalEntity pe2 = null;
		if (ip instanceof physicalEntity)
		{
			pe2 = (physicalEntity) ip;
		}
		else if (ip instanceof physicalEntityParticipant)
		{
			pe2 = ((physicalEntityParticipant) ip)
				.getPHYSICAL_ENTITY();
		}
		if (pe2 != null)
		{
			createInteraction(pe, pe2, interactionSet, type, interaction);
		}
	}

	private void createInteraction(physicalEntity pe,
                                   physicalEntity pe2,
                                   Set<SimpleInteraction> set,
                                   BinaryInteractionType type, interaction interaction)
	{
		if (!pe2.equals(pe))
		{
            SimpleInteraction si = new SimpleInteraction(pe, pe2, type);
            si.extractPublications(interaction);
            set.add(si);
		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(REACTS_WITH,
                INTERACTS_WITH);
	}

}
