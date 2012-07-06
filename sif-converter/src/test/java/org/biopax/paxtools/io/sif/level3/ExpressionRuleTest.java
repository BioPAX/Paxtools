package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.impl.level3.Mock;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * @author Ozgun Babur
 */
public class ExpressionRuleTest
{
	@Test
	public void testExpressionRule()
	{
		Mock mock = new Mock();

		Protein[] p = mock.create(Protein.class, 4);
		ProteinReference[] pr = mock.create(ProteinReference.class, 4);
		mock.bindArrays("entityReference", p, pr);

		TemplateReaction[] tr = mock.create(TemplateReaction.class, 2);

		mock.bindInPairs("product",
			tr[0], p[1],
			tr[1], p[3]);

		TemplateReactionRegulation[] trr = mock.create(TemplateReactionRegulation.class, 2);
		mock.bindInPairs("controller", 
			trr[0], p[0],
			trr[1], p[2]);
		mock.bindArrays("controlled", trr, tr);

		trr[0].setControlType(ControlType.ACTIVATION);
		trr[1].setControlType(ControlType.INHIBITION);

		HashMap options = new HashMap();
		options.put(BinaryInteractionType.UPREGULATE_EXPRESSION, true);
		options.put(BinaryInteractionType.DOWNREGULATE_EXPRESSION, true);
		ExpressionRule rule = new ExpressionRule();
		SimpleInteractionConverter sic = new SimpleInteractionConverter(options, rule);
		InteractionSetL3 interactions = (InteractionSetL3) sic.inferInteractions(mock.model);

		List<SimpleInteraction> expected =
			Arrays.asList(
				new SimpleInteraction(pr[0], pr[1], BinaryInteractionType.UPREGULATE_EXPRESSION),
				new SimpleInteraction(pr[2],pr[3], BinaryInteractionType.DOWNREGULATE_EXPRESSION));
		assertTrue(interactions.containsAll(expected));
	}
}
