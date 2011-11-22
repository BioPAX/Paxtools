package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.INTERACTS_WITH;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.REACTS_WITH;

/**
 * @author Emek Demir
 * @author Ozgun Babur
 */
public class ParticipatesRule extends InteractionRuleL3Adaptor
{

	private final Log log = LogFactory.getLog(ParticipatesRule.class);

	private static final List<BinaryInteractionType> binaryInteractionTypes =
			Arrays.asList(BinaryInteractionType.INTERACTS_WITH, REACTS_WITH);

	private boolean skipConversions;

	private boolean skipInteractions;

	@Override public void initOptionsNotNull(Map options)
	{
		super.initOptionsNotNull(options);
		skipConversions = options.containsKey(REACTS_WITH) && options.get(REACTS_WITH).equals(false);

		skipInteractions = options.containsKey(INTERACTS_WITH) && options.get(INTERACTS_WITH).equals(false);
	}

	public void inferInteractionsFromPE(InteractionSetL3 interactionSet, PhysicalEntity pe, Model model)
	{
		for (Interaction interaction : pe.getParticipantOf())
		{
			BinaryInteractionType type = getType(interaction);


			for (Entity participant : interaction.getParticipant())
			{
				processParticipant(interactionSet, participant, type, interaction);
			}
		}
	}

	private BinaryInteractionType getType(Interaction interaction)
	{
		if (interaction instanceof Conversion)
		{
			if (!skipConversions)
			{
				return REACTS_WITH;
			}
		} else if (interaction instanceof Control)
		{
			return null;
		} else if (!skipInteractions)
		{
			return INTERACTS_WITH;
		}
		return null;
	}

	private void processParticipant(InteractionSetL3 interactionSet, Entity entity, BinaryInteractionType type,
			Interaction interaction)
	{
		if (entity instanceof PhysicalEntity)
		{
			BioPAXElement source = this.getEntityReferenceOrGroup((PhysicalEntity) entity, interactionSet);

			for (Entity participant : interaction.getParticipant())
			{
				if (participant instanceof PhysicalEntity)

				{
					BioPAXElement target = this.getEntityReferenceOrGroup((PhysicalEntity) participant,
					                                                      interactionSet);
					createInteraction(source, target, interactionSet, type, interaction);
				}
			}
		}
	}

	private void createInteraction(BioPAXElement er1, BioPAXElement er2, InteractionSet set,
			BinaryInteractionType type,
			Interaction interaction)
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
		return binaryInteractionTypes;
	}
}