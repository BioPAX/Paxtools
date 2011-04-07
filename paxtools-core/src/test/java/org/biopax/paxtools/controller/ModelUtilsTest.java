package org.biopax.paxtools.controller;

import static org.junit.Assert.*;

import java.util.Collections;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Before;
import org.junit.Test;

public class ModelUtilsTest {

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
		pr1.addXref(x1);
		pr1.setDisplayName("pr1");
		x1.setId("1");
		x1.setDb("geneid");
		
		ProteinReference pr2 = factory.create(ProteinReference.class, "pr2");
		UnificationXref x2 = factory.create(UnificationXref.class, "x2");
		p2.setEntityReference(pr2);
		p2.setDisplayName("p2");
		pr2.addXref(x2);
		pr2.setDisplayName("pr2");
		x2.setId("2");
		x2.setDb("geneid");
		
		m.add(c);
		m.add(p1);
		m.add(p2);
		assertEquals(3, m.getObjects().size());
		assertEquals(2, c.getComponent().size());
		System.out.println("(before) components: " + c.getComponent().toString());
		
		// only 3 elements were added explicitly, but they have dependents -
		m.repair();
		
		assertEquals(7, m.getObjects().size());
		assertTrue(m.contains(x1));
		assertTrue(m.contains(x2));
		
		// create a new protein (the replacement)
		Protein p3 = factory.create(Protein.class, "p3");
		ProteinReference pr3 = factory.create(ProteinReference.class, "pr3");
		pr3.addXref(x2); // intentionally use same xref!
		pr3.setDisplayName("pr3");
		p3.setEntityReference(pr3);
		
		// do replace (replaces one element); 
		m.replace(p2, p3);	
		
		System.out.println("(after) components: " + c.getComponent().toString());
		assertEquals(7, m.getObjects().size()); // unchanged!
		assertEquals(2, c.getComponent().size());
		assertTrue(m.contains(p3)); // added!
		assertFalse(m.contains(p2)); // removed!
		
		//old children are still there
		assertTrue(m.contains(pr1));
		assertTrue(m.contains(pr2));
		assertTrue(m.contains(x1));
		assertTrue(m.contains(x2));
		
		// new children - not yet
		assertFalse(m.contains(pr3));
		
		// now - repair will add new children
		m.repair();
		assertEquals(8, m.getObjects().size()); // + pr3
		assertTrue(m.contains(pr3)); // added!
		assertTrue(m.contains(pr2)); // not deleted (may be dangling now)!
		assertTrue(m.contains(x2)); // not deleted (may be dangling now)!
		assertTrue(m.contains(pr1)); // not deleted (may be dangling now)!
		assertTrue(m.contains(x1)); // not deleted (may be dangling now)!
		
		// delete children of the replaced element if dangling
		ModelUtils mu = new ModelUtils(m);
		mu.removeDependentsIfDangling(p2);
		
		// pr2 - removed
		assertEquals(7, m.getObjects().size());
		assertFalse(m.contains(pr2)); // deleted!

		// ok to replace with null
		m.replace(p3, null);
		assertEquals(1, c.getComponent().size());
		mu.removeDependentsIfDangling(p3);
		assertFalse(m.contains(pr3)); // deleted!
		assertFalse(m.contains(x2)); // deleted!
		
		m.replace(pr1, x1); // logs error, no effect on the model
		assertTrue(m.contains(pr1));
		assertEquals(pr1, p1.getEntityReference());
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public final void testInferPropertyFromParent() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Provenance pro1 = model.addNew(Provenance.class, "urn:miriam:pid.pathway");
		Provenance pro2 = model.addNew(Provenance.class, "urn:miriam:signaling-gateway");
		Pathway pw1 = model.addNew(Pathway.class, "pathway");
		pw1.addDataSource(pro1);
		pw1.setStandardName("Pathway");
		Pathway pw2 = model.addNew(Pathway.class, "sub_pathway");
		pw2.setStandardName("Sub-Pathway");
		pw2.addDataSource(pro2);
		pw1.addPathwayComponent(pw2);
		ModelUtils mu = new ModelUtils(model);
		mu.inferPropertyFromParent("dataSource", Pathway.class);
		assertEquals(2, pw2.getDataSource().size());
		assertEquals(1, pw1.getDataSource().size());
		// TODO add a protein having empty dataSource and check it's still empty when Pathway.class used as a filter
	}
}
