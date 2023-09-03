package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;


/**
 */
public class UnionPropertyAccessorTest
{
	@Test
	public void union()
	{
		MockFactory factory = new MockFactory(BioPAXLevel.L3);

		Model model = factory.createModel();

		Protein p1 = model.addNew(Protein.class, "P1");
		ProteinReference r1 = model.addNew(ProteinReference.class, "R1");
		p1.setEntityReference(r1);

		Dna dna1 = model.addNew(Dna.class, "DNA1");

		DnaReference dnar1 = model.addNew(DnaReference.class, "DNAR1");

		dna1.setEntityReference(dnar1);

		BioSource human = model.addNew(BioSource.class, "human");
		human.setStandardName("Homo Sapiens");
		BioSource alien = model.addNew(BioSource.class, "alien");
		alien.setStandardName("Green Oval Heads");
		dnar1.setOrganism(human);
		r1.setOrganism(alien);

		Complex complex = model.addNew(Complex.class, "complex");
		complex.addComponent(dna1);
		complex.addComponent(p1);

		PathAccessor pa = new PathAccessor("Complex/component/entityReference/organism", BioPAXLevel.L3);
		Set valueFromBean = pa.getValueFromBean(complex);
		Assertions.assertTrue(valueFromBean.containsAll(Arrays.asList(human,alien)));
		Assertions.assertTrue(valueFromBean.size()==2);
	}
}
