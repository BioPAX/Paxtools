package org.biopax.paxtools.controller;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.SimpleIOHandlerTest;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
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
		
		
		Protein p1 = model.addNew(Protein.class, "p1"); // no dataSource
		Conversion conv1 = model.addNew(Conversion.class, "conv1"); // no dataSource
		pw1.addPathwayComponent(conv1);
		pw2.addPathwayComponent(conv1); //smoke/loop test: because hardly a conv1 (thus p1) can belong to 2 such pathways...
		conv1.addLeft(p1);
		
		ModelUtils mu = new ModelUtils(model);
		mu.inferPropertyFromParent("dataSource", Pathway.class); // apply to pathways only
		
		assertEquals(2, pw2.getDataSource().size());
		assertEquals(1, pw1.getDataSource().size());
		//a protein without dataSource must still have it empty (when Pathway.class used to filter)
		assertEquals(0, p1.getDataSource().size());
		
		mu.inferPropertyFromParent("dataSource"); // no filters
		
		assertEquals(2, pw2.getDataSource().size());
		assertEquals(1, pw1.getDataSource().size());
		assertEquals(2, p1.getDataSource().size()); // because conv1 belongs to pw1 and pw2!
	}
	
	
	@Test
	public final void testInferPropertyFromParent2() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Pathway pw1 = model.addNew(Pathway.class, "pathway1");
		BioSource mm = model.addNew(BioSource.class, "mouse");
		pw1.setOrganism(mm); // 
		Protein p1 = model.addNew(Protein.class, "p1"); 
		ProteinReference pr1 = model.addNew(ProteinReference.class, "pr1"); 
		p1.setEntityReference(pr1);
		Protein p2 = model.addNew(Protein.class, "p2"); 
		ProteinReference pr2 = model.addNew(ProteinReference.class, "pr2"); 
		p2.setEntityReference(pr2);
		BioSource hs = model.addNew(BioSource.class, "human");
		pr2.setOrganism(hs);
		Conversion conv1 = model.addNew(Conversion.class, "conv1");
		conv1.addLeft(p1);
		pw1.addPathwayComponent(conv1);
		pw1.setStandardName("Pathway1");
		Gene g1 = model.addNew(Gene.class, "gene1");
		PathwayStep s1 = model.addNew(PathwayStep.class, "step1");
		Catalysis ca1 = model.addNew(Catalysis.class, "catalysis1");
		ca1.addControlled(conv1);
		GeneticInteraction gi1 = model.addNew(GeneticInteraction.class, "gi1");
		pw1.addPathwayComponent(gi1);
		pw1.addPathwayComponent(ca1);
		pw1.addPathwayOrder(s1);
		gi1.addParticipant(g1);
		s1.addStepProcess(ca1);
		s1.addStepProcess(gi1);
		
		
		ModelUtils mu = new ModelUtils(model);
		mu.inferPropertyFromParent("organism", Gene.class, Pathway.class); // but not for SequenceEntityReference.class in this test ;)
		
		assertEquals(hs, pr2.getOrganism()); // still human, because it wasn't empty!
		assertEquals(mm, g1.getOrganism()); // inferred from the parent pathway!
		assertEquals(1, g1.getComment().size()); // - he-he, and a new comment was generated!
		assertNull(pr1.getOrganism()); // because ERs were filtered!
		
		mu.generateEntityOrganismXrefs();	
		//printModel(mu.getModel());
	}
	
	
	@Test
	public final void testA() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Provenance pro1 = model.addNew(Provenance.class, "urn:miriam:pid.pathway");
		Protein p1 = model.addNew(Protein.class, "p1"); 
		Pathway pw1 = model.addNew(Pathway.class, "pathway");
		Pathway pw2 = model.addNew(Pathway.class, "sub_pathway");
		Conversion conv1 = model.addNew(Conversion.class, "conv1");
		GeneticInteraction gi1 = model.addNew(GeneticInteraction.class, "gi1");
		Gene g1 = model.addNew(Gene.class, "gene1");
		
		pw1.addDataSource(pro1);
		pw1.setStandardName("Pathway");
		pw1.addPathwayComponent(pw2);
		pw1.addPathwayComponent(conv1);
		conv1.addLeft(p1);
		
		pw2.setStandardName("Sub-Pathway");
		pw2.addDataSource(pro1);
		pw2.addPathwayComponent(gi1);
		gi1.addParticipant(g1);
		
		ModelUtils mu = new ModelUtils(model);
		mu.generateEntityProcessXrefs(Process.class);
		
		//printModel(model);
		
		assertEquals(4, model.getObjects(RelationshipXref.class).size()); //- for 2 pathways and 2 interactions!
		assertEquals(1, model.getObjects(RelationshipTypeVocabulary.class).size());
		for(Entity e : model.getObjects(Entity.class)) {
			if(!e.equals(pw1))
				assertFalse(e.getXref().isEmpty());
		}
	}
	
	private void printModel(Model model) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		new SimpleIOHandler().convertToOWL(model, bytes);
		System.out.println(bytes.toString());
	}

	@Test
	public final void testMetrics()
	{
		Model model = SimpleIOHandlerTest.getL3Model(new SimpleIOHandler());
		ModelUtils mu = new ModelUtils(model);
		Map<Class<? extends BioPAXElement>,Integer> metrics =
				mu.generateClassMetrics();
		System.out.println("metrics = " + metrics);

	}
}
