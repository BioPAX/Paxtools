package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 */
public class SimpleInteractionTest
{
	@Test
	public void testEquals()
	{
		Model model = new MockFactory(BioPAXLevel.L3).createModel();
		ProteinReference p1 = model.addNew(ProteinReference.class, "p1");
		ProteinReference p2 = model.addNew(ProteinReference.class, "p2");
		SimpleInteraction si1 = new SimpleInteraction(p1, p2, BinaryInteractionType.INTERACTS_WITH);
		SimpleInteraction si2 = new SimpleInteraction(p1, p2, BinaryInteractionType.INTERACTS_WITH);
		assertThat(si1, is(si2));

		si1 = new SimpleInteraction(p2, p1, BinaryInteractionType.INTERACTS_WITH);
		si2 = new SimpleInteraction(p1, p2, BinaryInteractionType.INTERACTS_WITH);
		assertThat(si1, is(si2));


		si1 = new SimpleInteraction(p2, p1, BinaryInteractionType.STATE_CHANGE);
		si2 = new SimpleInteraction(p1, p2, BinaryInteractionType.STATE_CHANGE);
		assertThat(si1, not(si2));

		si1 = new SimpleInteraction(p1, p2, BinaryInteractionType.INTERACTS_WITH);
		si2 = new SimpleInteraction(p1, p2, BinaryInteractionType.COMPONENT_OF);
		assertThat(si1, not(si2));
	}
}
