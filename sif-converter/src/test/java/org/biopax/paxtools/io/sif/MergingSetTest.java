package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.impl.level3.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 */
public class MergingSetTest
{
	@Test
	public void testMergingAdd()
	{
		Model model = new MockFactory(BioPAXLevel.L3).createModel();
		ProteinReference p1 = model.addNew(ProteinReference.class, "p1");
		ProteinReference p2 = model.addNew(ProteinReference.class, "p2");
		MolecularInteraction mi = model.addNew(MolecularInteraction.class, "mi");
		MolecularInteraction mi2 = model.addNew(MolecularInteraction.class, "mi2");

		SimpleInteraction si1 = new SimpleInteraction(p1, p2, BinaryInteractionType.INTERACTS_WITH);
		SimpleInteraction si2 = new SimpleInteraction(p1, p2, BinaryInteractionType.INTERACTS_WITH);
		si1.addMediator(mi);
		si2.addMediator(mi2);
		assertThat(si1, is(si2));

		MergingSet ms = new MergingSet();
		ms.add(si1);
		ms.add(si2);
		assertTrue(ms.size() == 1);
		assertTrue(si1.getMediators().contains(mi) && si1.getMediators().contains(mi2));
	}
}
