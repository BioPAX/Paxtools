package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.SimpleIOHandlerTest;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.biopax.paxtools.util.SetEquivalenceChecker;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ModelUtilsTest {

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
		assertTrue(m.contains(p3)); // added!
		assertFalse(m.contains(p2)); // removed!		

		assertEquals(7, m.getObjects().size()); // unchanged!
		assertEquals(2, c.getComponent().size());
		
		assertTrue(c.getComponent().contains(p1)); // untouched!
		assertTrue(c.getComponent().contains(p3)); // new!
		assertFalse(c.getComponent().contains(p2)); 
		
		//old children are still there
		assertTrue(m.contains(pr1));
		assertTrue(m.contains(pr2));
		assertTrue(m.contains(x1));
		assertTrue(m.contains(x2));
		
		// new children - not yet
		// (replace is 'shallow', does not affect children of replaced objects)
		assertFalse(m.contains(pr3));
		
		// now - repair will find all new children
		m.repair();
		
		assertEquals(8, m.getObjects().size()); // + pr3
		assertTrue(m.contains(pr3)); // added!
		
		assertTrue(m.contains(pr2)); // not deleted (may be dangling now)!
		assertTrue(m.contains(x2)); // not deleted (may be dangling now)!
		assertTrue(m.contains(pr1)); // not deleted (may be dangling now)!
		assertTrue(m.contains(x1)); // not deleted (may be dangling now)!
		
		// delete dangling
		ModelUtils.removeObjectsIfDangling(m, ProteinReference.class);
		
		// pr2 - removed
		assertEquals(7, m.getObjects().size());
		assertFalse(m.contains(pr2)); // deleted!

		// ok to replace with null
		m.replace(p3, null);
		assertEquals(1, c.getComponent().size());
		ModelUtils.removeObjectsIfDangling(m, UtilityClass.class);
		assertFalse(m.contains(pr3)); // deleted!
		assertFalse(m.contains(x2)); // deleted!
		
		try {
			m.replace(pr1, x1); // fails, no effect on the model
			fail("Must fail!");
		} catch (IllegalBioPAXArgumentException e) {
			// fine!
		}
		// m is unchanged
		assertTrue(m.contains(pr1));
		assertEquals(pr1, p1.getEntityReference());
	}


	private void printModel(Model model) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		new SimpleIOHandler().convertToOWL(model, bytes);
		System.out.println(bytes.toString());
	}

	public final void printMetrics()
	{
		Model model = SimpleIOHandlerTest.getL3Model(new SimpleIOHandler());
		Map<Class<? extends BioPAXElement>,Integer> metrics =
				ModelUtils.generateClassMetrics(model);
		System.out.println("metrics = " + metrics);
	}
	

	@Test
	public final void testRemoveObjectsIfDangling() {
		Model model = (new SimpleIOHandler()).convertFromOWL(
			getClass().getClassLoader().getResourceAsStream("L3" + File.separator + "hcyc.owl"));
		assertEquals(6, model.getObjects().size());
		
		//there is no dangling util. class objects at the beginning
		ModelUtils.removeObjectsIfDangling(model, UtilityClass.class);
		assertEquals(6, model.getObjects().size());
		
		SmallMolecule sm1 = (SmallMolecule) model.getByID("SM1");
		SmallMoleculeReference smr1 = (SmallMoleculeReference) model.getByID("SMR1");
		SmallMoleculeReference a = (SmallMoleculeReference) model.getByID("A");
		
		assertNotNull(smr1);
		assertTrue(smr1.getMemberEntityReference().contains(a));
		
		// removing just SMR1 from SM1 and model makes both it and A dangling (though A is still member of SMR1)!
		sm1.setEntityReference(null); //unklinks SMR1
		model.remove(smr1);
		assertTrue(smr1.getEntityReferenceOf().isEmpty());
		
		// call to remove dangling things (but not B, which also belongs from SMR2, SM2)
		ModelUtils.removeObjectsIfDangling(model, UtilityClass.class);
		
		assertNull(model.getByID("SMR1"));		
		assertNull(model.getByID("A"));	
		SmallMoleculeReference b = (SmallMoleculeReference) model.getByID("B");
		assertNotNull(b);
		assertEquals(4, model.getObjects().size());
		
		// IMPORTANT to understand is that removed SMR1 and A still have B:
		assertEquals(3, b.getMemberEntityReferenceOf().size());
		assertTrue(a.getMemberEntityReference().contains(b));
		assertTrue(smr1.getMemberEntityReference().contains(b));
	}
	
	@Test
	public void testGenericNormalization()
	{
		MockFactory mock = new MockFactory(BioPAXLevel.L3);
		Model model = mock.createModel();
		Protein[] p = mock.create(model, Protein.class, 3);
		ProteinReference[] pr = mock.create(model, ProteinReference.class, 2);

		mock.bindArrays("entityReference", Arrays.copyOfRange(p, 0, 2), pr);

		mock.bindInPairs("memberPhysicalEntity", p[2],p[0], p[2],p[1]);

		ModelUtils.normalizeGenerics(model);

		assertThat(true, is(p[2].getEntityReference()!=null));
		assertThat(true, is(p[2].getEntityReference().getMemberEntityReference().contains(pr[0])));
		assertThat(true, is(p[2].getEntityReference().getMemberEntityReference().contains(pr[1])));
	}

    @Test
    public void testFixEquivalentFeatures()
    {
        MockFactory mock = new MockFactory(BioPAXLevel.L3);
        Model model = mock.createModel();
	    SequenceSite[] ss= mock.create(model, SequenceSite.class,1);
	    ss[0].setSequencePosition(0);
	    ss[0].setPositionStatus(PositionStatusType.EQUAL);

	    ModificationFeature[] mf = mock.create(model, ModificationFeature.class, 2);
	    mf[0].setFeatureLocation(ss[0]);
	    mf[1].setFeatureLocation(ss[0]);

	    mf[0].setFeatureLocation(ss[0]);
	    mf[1].setFeatureLocation(ss[0]);

	    ProteinReference[] pr = mock.create(model, ProteinReference.class, 1);
	    pr[0].addEntityFeature(mf[0]);
	    pr[0].addEntityFeature(mf[1]);

	    ModelUtils.replaceEquivalentFeatures(model);
	    assertTrue(model.getObjects(ModificationFeature.class).size()==1);
    }
    
	@Test
	public void testMergeEquivalentConversions()
	{
		MockFactory mock = new MockFactory(BioPAXLevel.L3);
		Model model = mock.createModel();

		ProteinReference[] pr = mock.create(model, ProteinReference.class, 2);

		Protein[] proteins = mock.create(model, Protein.class, 4);

		mock.bindInPairs("entityReference", proteins[0], pr[0], proteins[1], pr[0], proteins[2], pr[1], proteins[3], pr[1]);

		BiochemicalReaction[] rxn = mock.create(model, BiochemicalReaction.class, 2);

		mock.bindInPairs("left", rxn[0], proteins[2], rxn[1], proteins[2]);
		mock.bindInPairs("right", rxn[0], proteins[3], rxn[1], proteins[3]);

		Catalysis[] ctl = mock.create(model, Catalysis.class,2);
		mock.bindArrays("controlled", ctl, rxn);
		mock.bindInPairs("controller", ctl[0], proteins[0], ctl[1], proteins[1]);

		ModelUtils.mergeEquivalentInteractions(model);

		assertTrue(model.contains(rxn[0])^model.contains(rxn[1]));
	}

	@Test
	public void testMergeEquivalentPhysicalEntities()
	{
		MockFactory mock = new MockFactory(BioPAXLevel.L3);
		Model model = mock.createModel();

		ProteinReference[] pr = mock.create(model, ProteinReference.class, 2);
		Protein[] proteins = mock.create(model, Protein.class, 4);
		mock.bindInPairs("entityReference", proteins[0], pr[0], proteins[1], pr[0], proteins[2], pr[1], proteins[3], pr[1]);

		//generate two semantically equivalent reactions
		BiochemicalReaction[] rxn = mock.create(model, BiochemicalReaction.class, 2);
		mock.bindInPairs("left", rxn[0], proteins[2], rxn[1], proteins[2]);
		mock.bindInPairs("right", rxn[0], proteins[3], rxn[1], proteins[3]);

		Catalysis[] ctl = mock.create(model, Catalysis.class,2);
		mock.bindArrays("controlled", ctl, rxn);
		mock.bindInPairs("controller", ctl[0], proteins[0], ctl[1], proteins[1]);

		ModelUtils.mergeEquivalentPhysicalEntities(model);

		//either 0 or 1 kept by chance (and the other gets replaced)
		assertTrue(model.contains(proteins[0])^model.contains(proteins[1]));
		assertTrue(model.contains(proteins[2])^model.contains(proteins[3]));
		assertTrue(ctl[1].getController().contains(proteins[0])^ctl[1].getController().contains(proteins[1]));
		assertTrue(ctl[1].getController().contains(proteins[0])^ctl[1].getController().contains(proteins[1]));
		assertEquals(1, rxn[0].getRight().size());
		assertEquals(1, rxn[1].getRight().size());
		assertEquals(1, rxn[0].getLeft().size());
		assertEquals(1, rxn[1].getLeft().size());
		if(model.contains(proteins[2])) {
			assertTrue(rxn[0].getRight().contains(proteins[2]));
			assertTrue(rxn[1].getRight().contains(proteins[2]));
			assertTrue(rxn[0].getLeft().contains(proteins[2]));
			assertTrue(rxn[1].getLeft().contains(proteins[2]));
		} else if(model.contains(proteins[3])) {
			assertTrue(rxn[0].getRight().contains(proteins[3]));
			assertTrue(rxn[1].getRight().contains(proteins[3]));
			assertTrue(rxn[0].getLeft().contains(proteins[3]));
			assertTrue(rxn[1].getLeft().contains(proteins[3]));
		}

		//merge interactions and test it
		ModelUtils.mergeEquivalentInteractions(model);
		assertTrue(model.contains(rxn[0])^model.contains(rxn[1]));
	}

	@Test
	public void testBase62() {
		final String longUri = "http://www.ctdbase.org/#process_RXN_%5BCFTR+protein+inhibits+the+reaction+%5B%5B" +
				"Colforsin+co-treated+with+Genistein%5D+results+in+increased+transport+of+Iodides%5D%5D";
		String shortStr = ModelUtils.encodeBase62(longUri);
		Assert.assertEquals("k9viXaDIiOW", shortStr);
		String shortUri = ModelUtils.shortenUri("http://www.ctdbase.org/", longUri);
		Assert.assertEquals("http://www.ctdbase.org/k9viXaDIiOW", shortUri);
	}

	@Test
	public final void testSerializeDeserializeModel() throws Exception {
		SimpleIOHandler io = new SimpleIOHandler();
		Model m1 = io.convertFromOWL(SimpleIOHandlerTest.class.getClassLoader()
				.getResourceAsStream("L3" + File.separator + "biopax3-short-metabolic-pathway.owl"));
		byte[] s1 = ModelUtils.serialize(m1);
		Model m2 = (Model) ModelUtils.deserialize(s1);

		assertEquals(m1.size(), m2.size());
		assertTrue(SetEquivalenceChecker.isEquivalent(m1.getObjects(), m2.getObjects()));

		//REM: if we'd write/read m1,m2 to/from string/bytes and then compare,
		//it would not match as the elements processing order is not guaranteed ;)
	}
}
