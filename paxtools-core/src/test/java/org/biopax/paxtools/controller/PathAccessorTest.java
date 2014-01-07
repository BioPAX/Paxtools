package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PathAccessorTest
{
	@Test
	public void testPaths()
	{
		MockFactory mock= new MockFactory(BioPAXLevel.L3);
		final Model model = mock.createModel();
		
		Protein[] p = mock.create(model, Protein.class, 2);
		Protein[] member = mock.create(model, Protein.class, 1,"member");
		ProteinReference[] pr = mock.create(model, ProteinReference.class, 2);
		PublicationXref[] px = mock.create(model, PublicationXref.class, 2);
		Complex[] c = mock.create(model, Complex.class, 3);
		SmallMoleculeReference smr[] = mock.create(model, SmallMoleculeReference.class,1);
		SmallMolecule sm[] = mock.create(model, SmallMolecule.class,1);

		BiochemicalReaction[] rx = mock.create(model, BiochemicalReaction.class,1);
		Pathway[] pw = mock.create(model,Pathway.class,1);
		BiochemicalPathwayStep[] pws = mock.create(model,BiochemicalPathwayStep.class,1);

		pws[0].setStepConversion(rx[0]);

		mock.bindArrays(mock.editor("entityReference", Protein.class), p, pr);
		mock.bindArrays(mock.editor("entityReference", SmallMolecule.class), sm, smr);
		mock.bindArrays(mock.editor("xref",ProteinReference.class),pr,px);

		mock.bindInPairs(mock.editor("component",Complex.class),
		                 c[0],c[1],
		                 c[1],c[2],
		                 c[2],member[0]);


		PathAccessor accessor = new PathAccessor("Protein/entityReference/xref:PublicationXref", BioPAXLevel.L3);
		Set values = accessor.getValueFromBean(p[0]);
		assertTrue(values.contains(px[0]) && values.size() == 1);
		values = accessor.getValueFromModel(model);
		assertTrue(values.containsAll(Arrays.asList(px)) && values.size() == 2);

		accessor = new PathAccessor("Conversion/stepProcessOf", BioPAXLevel.L3);
		values = accessor.getValueFromBean(rx[0]);
		assertTrue(values.contains(pws[0]) && values.size() == 1);


		accessor = new PathAccessor("Protein/entityReference/xref:RelationshipXref", BioPAXLevel.L3);
		values = accessor.getValueFromBean(p[1]);
		assertFalse(values.contains(px[1]) && values.size() == 1);


		accessor = new PathAccessor("PublicationXref/xrefOf", BioPAXLevel.L3);
		values = accessor.getValueFromBean(px[1]);
		assertTrue(values.contains(pr[1]) && values.size()==1);

		accessor = new PathAccessor("Complex/component*/name", BioPAXLevel.L3);
		values = accessor.getValueFromBean(c[0]);
		assertTrue(values.containsAll(c[1].getName()));
		assertTrue(values.containsAll(c[2].getName()));
		assertTrue(values.containsAll(member[0].getName()));


		accessor = new PathAccessor("Protein/cellularLocation", BioPAXLevel.L3);
		values = accessor.getValueFromBean(p[0]);
		assertTrue(accessor.isUnknown(values));

		PathAccessor mwAccessor = new PathAccessor("SmallMoleculeReference/molecularWeight", BioPAXLevel.L3);
		values = mwAccessor.getValueFromBean(smr[0]);
		assertTrue(mwAccessor.isUnknown(values));
		
		accessor = new PathAccessor("Protein/entityReference", BioPAXLevel.L3);
		Collection<BioPAXElement> beans = new ArrayList<BioPAXElement>();
		beans.add(p[0]);
		beans.add(sm[0]);
		values = accessor.getValueFromBeans(beans);
		assertEquals(1, values.size());
		assertTrue(values.contains(pr[0]));

        	accessor = new PathAccessor("SmallMolecule/entityReference", BioPAXLevel.L3);
	        values = accessor.getValueFromBeans(beans);
	        assertEquals(1, values.size());
	        assertTrue(values.contains(smr[0]));
	}
}
