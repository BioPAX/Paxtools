package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.level3.MockFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 *
 */
public class PathAccessorTest
{
	@Test
	public void testPaths()
	{
		Model model = new MockFactory(BioPAXLevel.L3).createModel();
		Protein p1 = model.addNew(Protein.class, "P1");
		PublicationXref px = model.addNew(PublicationXref.class, "Px1");
		px.setId("myId");
		RelationshipXref rx = model.addNew(RelationshipXref.class, "rx1");
		rx.setId("yourID");
		ProteinReference r1 = model.addNew(ProteinReference.class, "R1");
		r1.addXref(px);
		r1.addXref(rx);
		p1.setEntityReference(r1);

		Protein p2 = model.addNew(Protein.class, "P2");
		PublicationXref px2 = model.addNew(PublicationXref.class, "Px2");
		px2.setId("hisId");
		RelationshipXref rx2 = model.addNew(RelationshipXref.class, "rx2");
		rx2.setId("herID");
		ProteinReference r2 = model.addNew(ProteinReference.class, "R2");

		p2.setEntityReference(r2);
		r2.addXref(px2);
		r2.addXref(rx2);

		Complex outer = model.addNew(Complex.class, "outer");
		Complex inner = model.addNew(Complex.class, "inner");
		Complex innermost = model.addNew(Complex.class, "innermost");
		Protein base = model.addNew(Protein.class, "base");

		outer.addComponent(inner);
		inner.addComponent(innermost);
		innermost.addComponent(base);


		PathAccessor accessor = new PathAccessor("Protein/entityReference/xref:PublicationXref", BioPAXLevel.L3);
		Set values = accessor.getValueFromBean(p1);
		assertEquals(new ClassFilterSet(r1.getXref(), PublicationXref.class).size(), values.size());


		accessor = new PathAccessor("Protein/entityReference/xref:RelationshipXref", BioPAXLevel.L3);
		values = accessor.getValueFromBean(p1);
		assertEquals(new ClassFilterSet(r1.getXref(), RelationshipXref.class).size(), values.size());

		accessor = new PathAccessor("Protein/entityReference/xref:RelationshipXref/id", BioPAXLevel.L3);
		values = accessor.getValueFromBean(p1);
		assertTrue(values.contains("yourID"));

		accessor = new PathAccessor("PublicationXref/xrefOf", BioPAXLevel.L3);
		values = accessor.getValueFromBean(px2);
		assertTrue(values.contains(r2));

		accessor = new PathAccessor("Complex/component*/name", BioPAXLevel.L3);
		values = accessor.getValueFromBean(outer);
		assertThat(true, is(values.size() == 18));
		assertThat(true, is(values.containsAll(base.getName())));
		assertThat(true, is(values.containsAll(inner.getName())));
		assertThat(true, is(values.containsAll(innermost.getName())));

		accessor = new PathAccessor("Protein/cellularLocation", BioPAXLevel.L3);
		values = accessor.getValueFromBean(p1);
		assertTrue(accessor.isUnknown(values));

		SmallMoleculeReference sm1 = model.addNew(SmallMoleculeReference.class, "SM1");
		PathAccessor mwAccessor = new PathAccessor("SmallMoleculeReference/molecularWeight", BioPAXLevel.L3);
		values = mwAccessor.getValueFromBean(sm1);
//		System.out.println("mw: " + values.toString());
		assertTrue(mwAccessor.isUnknown(values));
	}
}
