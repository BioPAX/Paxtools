package org.biopax.paxtools.fixer;

import org.biopax.paxtools.impl.level3.Mock;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.junit.Test;

import java.util.Arrays;

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

		mock.bindArrays("entityReference", Arrays.copyOfRange(p, 0, 2), pr);

		mock.bindInPairs("memberPhysicalEntity",
		                 p[2],p[0],
		                 p[2],p[1]);

		Fixer.normalizeGenerics(mock.model);

		assertThat(true, is(p[2].getEntityReference()!=null));
		assertThat(true, is(p[2].getEntityReference().getMemberEntityReference().contains(pr[0])));
		assertThat(true, is(p[2].getEntityReference().getMemberEntityReference().contains(pr[1])));



	}
}
