package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.INTERACTS_WITH;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.REACTS_WITH;

/**
 * Finds pairs of molecules that are participants of the same interaction for the INTERACTS_WITH
 * type, and participants of the same Conversion for the REACTS_WITH type.
 * @author Emek Demir
 * @author Ozgun Babur
 */
public class ParticipatesRule extends InteractionRuleL3Adaptor
{
	/**
	 * Log for logging.
	 */
	private final Log log = LogFactory.getLog(ParticipatesRule.class);

	/**
	 * Supported interaction types.
	 */
	private static final List<BinaryInteractionType> binaryInteractionTypes =
			Arrays.asList(BinaryInteractionType.INTERACTS_WITH, REACTS_WITH);

	/**
	 * Option to not to mine REACTS_WITH type.
	 */
	private boolean skipConversions;

	/**
	 * Option to not to mine INTERACTS_WITH type.
	 */
	private boolean skipInteractions;

	/**
	 * Initializes options.
	 * @param options options map
	 */
	@Override public void initOptionsNotNull(Map options)
	{
		skipConversions = checkOption(REACTS_WITH,Boolean.FALSE,options);
		skipInteractions = checkOption(INTERACTS_WITH,Boolean.FALSE,options);
	}

	/**
	 * Infers interactions starting from the given PhysicalEntity.
	 * @param interactionSet to be populated
	 * @param pe PhysicalEntity that will be the seed of the inference
	 * @param model BioPAX model
	 */
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

	/**
	 * Decides the type of the binary interaction according to the subclass of the Interaction.
	 * Conversion is used for REACTS_WITH type, Control is ignored, and others are used for
	 * INTERACTS_WITH type.
	 * @param interaction the mediator of the new binary interaction
	 * @return type of the new interaction
	 */
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

	/**
	 * Continues interaction creation with the given elements.
	 * @param interactionSet to be populated
	 * @param entity participant of the Interaction
	 * @param type type of the binary interaction
	 * @param interaction Interaction to use as mediator
	 */
	private void processParticipant(InteractionSetL3 interactionSet, Entity entity,
		BinaryInteractionType type, Interaction interaction)
	{
		if (entity instanceof PhysicalEntity)
		{
			BioPAXElement source = interactionSet.getGroupMap().getEntityReferenceOrGroup(entity);

			for (Entity participant : interaction.getParticipant())
			{
				if (participant instanceof PhysicalEntity)

				{
					BioPAXElement target = interactionSet.getGroupMap().getEntityReferenceOrGroup(participant);
					createAndAdd(source, target, interactionSet, type, interaction);
				}
			}
		}
	}

	/**
	 * Gets supported interaction types.
	 * @return supported interaction types
	 */
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}