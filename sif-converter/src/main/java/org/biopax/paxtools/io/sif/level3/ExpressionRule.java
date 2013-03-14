package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.c.ConBox;
import org.biopax.paxtools.pattern.c.LinkedPE;

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
	 * Pattern used for searching the transcriptional relation.
	 */
	protected static Pattern pattern;

	/**
	 * Constructs the pattern.
	 */
	static
	{
		pattern = new Pattern(7, PhysicalEntity.class);
		int i = 0;
		pattern.addConstraint(ConBox.peToER(), i, ++i);
		pattern.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i-1, ++i);
		pattern.addConstraint(ConBox.peToControl(), i, ++i);
		pattern.addConstraint(ConBox.controlToTempReac(), i, ++i);
		pattern.addConstraint(ConBox.product(), i, ++i);
		pattern.addConstraint(ConBox.peToER(), i, ++i);
	}

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
		for (Match match : Searcher.search(pe, pattern))
		{
			Control ctrl = (Control) match.get(3);
			BinaryInteractionType type =
				ctrl.getControlType() != null && ctrl.getControlType().toString().startsWith("I") ?
					BinaryInteractionType.DOWNREGULATE_EXPRESSION :
					BinaryInteractionType.UPREGULATE_EXPRESSION;

			interactionSet.add(new SimpleInteraction(match.get(1), match.get(6), type,
				match.get(3), match.get(4)));
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
