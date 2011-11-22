package org.biopax.paxtools.io.sif.level2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.MaximumInteractionThresholdExceedException;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.INTERACTS_WITH;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.REACTS_WITH;

/**
 * User: demir Date: Dec 28, 2007 Time: 5:52:06 PM
 */
public class ParticipatesRule extends InteractionRuleL2Adaptor
{
	private static List<BinaryInteractionType> binaryInteractionTypes =
			Arrays.asList(BinaryInteractionType.INTERACTS_WITH, REACTS_WITH);

	private long threshold;

	private static Log log = LogFactory.getLog(ComponentRule.class);

	boolean suppressExceptions = false;

	private boolean skipInteractions;

	private boolean skipConversions;

	public ParticipatesRule()
	{
		this(Integer.MAX_VALUE);
	}

	public ParticipatesRule(int threshold)
	{
		this(threshold, false);

	}

	public ParticipatesRule(int threshold, boolean suppressExceptions)
	{
		this.threshold = threshold;
		this.suppressExceptions = suppressExceptions;

	}


	@Override public void initOptionsNotNull(Map options)
	{
		skipConversions = options.containsKey(REACTS_WITH) && options.get(REACTS_WITH).equals(false);

		skipInteractions = options.containsKey(INTERACTS_WITH) && options.get(INTERACTS_WITH).equals(false);
	}

	public void inferInteractionsFromPE(InteractionSet interactionSet, physicalEntity pe, Model model)
	{


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
				continue;
			} else if (interaction instanceof conversion)
			{
				if (skipConversions)
				{
					continue;
				}

				type = REACTS_WITH;
			} else
			{
				if (skipInteractions)
				{
					continue;
				}

				type = INTERACTS_WITH;
			}

			Set<InteractionParticipant> ips = interaction.getPARTICIPANTS();
			if (ips.size() > threshold)
			{
				log.warn("The size of participants is too large! Skipping");
				if (suppressExceptions) return;
				else throw new MaximumInteractionThresholdExceedException(pe.toString());
			} else
			{
				for (InteractionParticipant ip : ips)
				{
					processParticipant(interactionSet, pe, ip, type, interaction);
				}
			}
		}
	}

	private void processParticipant(InteractionSet interactionSet, physicalEntity pe, InteractionParticipant ip,
			BinaryInteractionType type, interaction interaction)
	{
		physicalEntity pe2 = null;
		if (ip instanceof physicalEntity)
		{
			pe2 = (physicalEntity) ip;
		} else if (ip instanceof physicalEntityParticipant)
		{
			pe2 = ((physicalEntityParticipant) ip).getPHYSICAL_ENTITY();
		}
		if (pe2 != null)
		{
			createInteraction(pe, pe2, interactionSet, type, interaction);
		}
	}

	private void createInteraction(physicalEntity pe, physicalEntity pe2, InteractionSet set,
			BinaryInteractionType type, interaction interaction)
	{
		if (!pe2.equals(pe))
		{
			SimpleInteraction si = new SimpleInteraction(pe, pe2, type);
			si.addMediator(interaction);
			set.add(si);

		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}


}
