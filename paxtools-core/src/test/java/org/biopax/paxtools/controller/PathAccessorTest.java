package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.level3.Mock;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PathAccessorTest
{
	@Test
	public void testPaths()
	{
		Mock mock= new Mock();

		Protein[] p = mock.create(Protein.class, 2);
		Protein[] member = mock.create(Protein.class, 1,"member");
		ProteinReference[] pr = mock.create(ProteinReference.class, 2);
		PublicationXref[] px = mock.create(PublicationXref.class, 2);
		Complex[] c = mock.create(Complex.class, 3);
		SmallMoleculeReference smr[] = mock.create(SmallMoleculeReference.class,1);


		mock.bindArrays(mock.editor("entityReference", Protein.class), p, pr);
		mock.bindArrays(mock.editor("xref",ProteinReference.class),pr,px);


		mock.bindInPairs(mock.editor("component",Complex.class),
		                 c[0],c[1],
		                 c[1],c[2],
		                 c[2],member[0]);


		PathAccessor accessor = new PathAccessor("Protein/entityReference/xref:PublicationXref", BioPAXLevel.L3);
		Set values = accessor.getValueFromBean(p[0]);
		assertThat(true,is(values.contains(px[0]) && values.size()==1));
		values = accessor.getValueFromModel(mock.model);
		assertThat(true,is(values.containsAll(Arrays.asList(px)) && values.size()==2));

		accessor = new PathAccessor("Protein/entityReference/xref:RelationshipXref", BioPAXLevel.L3);
		values = accessor.getValueFromBean(p[1]);
		assertThat(false, is(values.contains(px[1]) && values.size() == 1));


		accessor = new PathAccessor("PublicationXref/xrefOf", BioPAXLevel.L3);
		values = accessor.getValueFromBean(px[1]);
		assertTrue(values.contains(pr[1]) && values.size()==1);

		accessor = new PathAccessor("Complex/component*/name", BioPAXLevel.L3);
		values = accessor.getValueFromBean(c[0]);
		assertThat(true, is(values.containsAll(c[1].getName())));
		assertThat(true, is(values.containsAll(c[2].getName())));
		assertThat(true, is(values.containsAll(member[0].getName())));


		accessor = new PathAccessor("Protein/cellularLocation", BioPAXLevel.L3);
		values = accessor.getValueFromBean(p[0]);
		assertTrue(accessor.isUnknown(values));

		PathAccessor mwAccessor = new PathAccessor("SmallMoleculeReference/molecularWeight", BioPAXLevel.L3);
		values = mwAccessor.getValueFromBean(smr[0]);
		assertTrue(mwAccessor.isUnknown(values));


	}
}
