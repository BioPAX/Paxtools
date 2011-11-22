package org.biopax.paxtools.fixer;

import org.biopax.paxtools.impl.level3.Mock;
import org.biopax.paxtools.impl.level3.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**

 */
public class FixerTest
{
	@Test
	public void testGenericNormalization()
	{
		Mock mock = new Mock();
		Protein[] p = mock.create(Protein.class, 3);
		ProteinReference[] pr = mock.create(ProteinReference.class, 2);

		p[1].setEntityReference(pr[1]);
		p[2].setEntityReference(pr[2]);

		p[3].addMemberPhysicalEntity(p[1]);
		p[3].addMemberPhysicalEntity(p[2]);


		MockFactory mf = new MockFactory(BioPAXLevel.L3);
		Model model = mf.createModel();
		Protein p1= model.addNew(Protein.class, "p1");
		Protein p2= model.addNew(Protein.class, "p2");
		Protein p3= model.addNew(Protein.class, "p3");

		ProteinReference pr1= model.addNew(ProteinReference.class, "pr1");
		ProteinReference pr2= model.addNew(ProteinReference.class, "pr2");

		p1.setEntityReference(pr1);
		p2.setEntityReference(pr2);

		p3.addMemberPhysicalEntity(p1);
		p3.addMemberPhysicalEntity(p2);

		Fixer.normalizeGenerics(model);

		assertThat(true, is(p3.getEntityReference()!=null));
		assertThat(true, is(p3.getEntityReference().getMemberEntityReference().contains(pr1)));
		assertThat(true, is(p3.getEntityReference().getMemberEntityReference().contains(pr1)));



	}
}
