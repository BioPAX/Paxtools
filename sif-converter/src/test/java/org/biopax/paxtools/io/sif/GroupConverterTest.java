package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.impl.level3.MockFactory;
import org.biopax.paxtools.io.sif.level3.GroupMap;
import org.biopax.paxtools.io.sif.level3.Grouper;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 */
public class GroupConverterTest
{
	@Test
	public void testGroups()
	{

		MockFactory mf = new MockFactory(BioPAXLevel.L3);
		Model model = mf.createModel();
		Protein p1 = model.addNew(Protein.class, "p1");
		Protein p2 = model.addNew(Protein.class, "p2");
		Protein p3 = model.addNew(Protein.class, "p3");

		ProteinReference pr1 = model.addNew(ProteinReference.class, "pr1");
		ProteinReference pr2 = model.addNew(ProteinReference.class, "pr2");

		p1.setEntityReference(pr1);
		p2.setEntityReference(pr2);

		p3.addMemberPhysicalEntity(p1);
		p3.addMemberPhysicalEntity(p2);
        GroupMap groupMap = Grouper.inferGroups(model); //this calls normalizeGenerics

        assertThat(true, is(groupMap.getMap().size() == 1));
	}
}
