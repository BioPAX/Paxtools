package org.biopax.paxtools.impl;

import static org.junit.Assert.*;

import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Before;
import org.junit.Test;

public class ModelImplTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testMergeAndReplace() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Model m = factory.createModel();
		
		Complex c = factory.create(Complex.class, "c1");
		Protein p1 = factory.create(Protein.class, "p1");
		Protein p2 = factory.create(Protein.class, "p2");
		ProteinReference pr1 = factory.create(ProteinReference.class, "pr1");
		UnificationXref x1 = factory.create(UnificationXref.class, "x1");
		
		c.addComponent(p1);
		c.addComponent(p2);
		c.setDisplayName("complex");
		p1.setEntityReference(pr1);
		p1.setDisplayName("p1");
		p2.setEntityReference(pr1);
		p2.setDisplayName("p1 copy");
		pr1.addXref(x1);
		pr1.setDisplayName("pr1");
		x1.setId("1");
		x1.setDb("geneid");
		
		m.add(c);
		m.add(p1);
		m.add(p2);
		assertEquals(3, m.getObjects().size());
		assertEquals(2, c.getComponent().size());
		// only 3 elements were added explicitly, but some have dependents!
		// find and merge them now:
		m.merge(m);
		assertEquals(5, m.getObjects().size());
		assertEquals(2, c.getComponent().size());
		System.out.println("components: " + c.getComponent().toString());
		System.out.println("Before the replace, " +
				"no. objects in the model=" + m.getObjects().size());
		System.out.println("components: " + c.getComponent().toString());
		
		// create a new protein (replacement)
		Protein p3 = factory.create(Protein.class, "p3");
		ProteinReference pr3 = factory.create(ProteinReference.class, "pr3");
		UnificationXref x2 = factory.create(UnificationXref.class, "x2");
		x2.setId("2");
		x2.setDb("geneid");
		pr3.addXref(x2);
		pr3.setDisplayName("pr3");
		p3.setEntityReference(pr3);
		
		assertTrue(m.contains(x1));
		assertFalse(m.contains(x2));
		
		// do replace!
		m.replace(p2, p3);	
		
		System.out.println("After the replace, " +
				"no. objects in the model=" + m.getObjects().size());
		System.out.println("components: " + c.getComponent().toString());
		assertEquals(2, c.getComponent().size());
		assertTrue(m.contains(p3)); // added!
		assertFalse(m.contains(p2)); // removed!
		assertTrue(m.contains(pr3)); // added!
		assertTrue(m.contains(x2)); // added!
		assertTrue(m.contains(x1)); // not deleted (may be dangling now)!
		// p2 - removed (children kept), p3, pr3, x2 - added
		assertEquals(7, m.getObjects().size());

		m.replace(p3, null);
		assertEquals(1, c.getComponent().size());
		//assertEquals(6, m.getObjects().size());
		
		m.replace(pr1, x1); // logs error, no effect on the model
		assertTrue(m.contains(pr1));
		assertEquals(pr1, p1.getEntityReference());
	}

}
