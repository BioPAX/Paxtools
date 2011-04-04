package org.biopax.paxtools.controller;

import static org.junit.Assert.*;

import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.*;
import org.junit.Before;
import org.junit.Test;

public class ShallowCopyTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testCopyTString() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Protein p1 = factory.create(Protein.class, "p1");
		ProteinReference pr1 = factory.create(ProteinReference.class, "pr1");
		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		UnificationXref x2 = factory.create(UnificationXref.class, "x2");
		p1.setEntityReference(pr1);
		p1.setDisplayName("p1");
		pr1.addXref(x1);
		pr1.addXref(x2);
		pr1.setDisplayName("pr1");
		x1.setId("1");
		x1.setDb("geneid");
		x2.setId("2");
		x2.setDb("geneid");
		
		assertEquals(pr1, p1.getEntityReference());
		assertEquals(2, p1.getEntityReference().getXref().size());
		
		Protein p2 = (new ShallowCopy()).copy(p1, "p1");
		
		assertEquals(p1.getRDFId(), p2.getRDFId());
		assertFalse(p1 == p2);
		assertTrue(p1.isEquivalent(p2));
		assertEquals(pr1, p2.getEntityReference());
		assertEquals(2, p2.getEntityReference().getXref().size());
	}

}
