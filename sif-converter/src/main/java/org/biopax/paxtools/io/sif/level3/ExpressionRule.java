package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import java.util.Arrays;
import java.util.List;

/**
 * This rule mines the transctivation and transinhibition relations between entities.
 *
 * @author Ozgun Babur
 */
public class ExpressionRule extends InteractionRuleL3Adaptor
{
	/**
	 * Log for logging.
	 */
	private final Log log = LogFactory.getLog(ParticipatesRule.class);

	/**
	 * Types of binary interactions that this class can generate.
	 */
	private static final List<BinaryInteractionType> binaryInteractionTypes =
		Arrays.asList(BinaryInteractionType.UPREGULATE_EXPRESSION, 
			BinaryInteractionType.DOWNREGULATE_EXPRESSION);

	/**
	 * Searches the transcriptional relations using the pattern, then decides the interaction type
	 * according to the type of the Control.
	 * @param interactionSet to be populated
	 * @param pe PhysicalEntity that will be the seed of the inference
	 * @param model BioPAX model
	 */
	@Override
	public void inferInteractionsFromPE(InteractionSetL3 interactionSet, PhysicalEntity pe, 
		Model model)
	{
		for (Control ctrl : pe.getControllerOf())
		{
			BinaryInteractionType type = ctrl.getControlType() != null &&
				ctrl.getControlType().toString().startsWith("I") ?
				BinaryInteractionType.DOWNREGULATE_EXPRESSION :
				BinaryInteractionType.UPREGULATE_EXPRESSION;

			if (ctrl instanceof TemplateReactionRegulation)
			{
				for (Process process : ctrl.getControlled())
				{
					if (process instanceof TemplateReaction)
					{
						TemplateReaction tr = (TemplateReaction) process;
						for (PhysicalEntity prod : tr.getProduct())
						{
							createAndAdd(interactionSet.getGroupMap().getEntityReferenceOrGroup(pe),
								interactionSet.getGroupMap().getEntityReferenceOrGroup(prod),
								interactionSet, type, ctrl, tr);
						}
					}
				}
			}
		}
	}

	/**
	 * Gets a list of the rule types that this class implements.
	 * @return supported rules
	 */
	@Override
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}

}
