package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.impl.level3.MockFactory;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

/**

 */
public class GenericsRuleTest
{
	@Test
	public void testInferInteractions() throws Exception
	{
		Model model = new MockFactory(BioPAXLevel.L3).createModel();

		ProteinReference generic = model.addNew(ProteinReference.class, "Generic");
		ProteinReference member = model.addNew(ProteinReference.class, "Member1");

		generic.addMemberEntityReference(member);

		SimpleInteractionConverter sic = new SimpleInteractionConverter(new GenericsRule());
		Set<SimpleInteraction> simpleInteractions = sic.inferInteractions(model);

		assertTrue(simpleInteractions.size() == 1);
		SimpleInteraction next = simpleInteractions.iterator().next();
		assertTrue(next.getSource() == generic);
		assertTrue(next.getTarget() == member);
	}
}
