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
 * @author Ozgun Babur
 */
public class ExpressionRule extends InteractionRuleL3Adaptor
{
	private final Log log = LogFactory.getLog(ParticipatesRule.class);

	private static final List<BinaryInteractionType> binaryInteractionTypes =
		Arrays.asList(BinaryInteractionType.UPREGULATE_EXPRESSION, 
			BinaryInteractionType.DOWNREGULATE_EXPRESSION);

	protected static Pattern pattern;
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

	@Override
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}

}
