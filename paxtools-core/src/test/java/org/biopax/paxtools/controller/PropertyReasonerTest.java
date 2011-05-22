package org.biopax.paxtools.controller;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Before;
import org.junit.Test;

public class PropertyReasonerTest {

	Model model;
	Provenance pro1, pro2;
	Pathway pw1, pw2;
	Protein p1, p2;
	Conversion conv1;
	BioSource mm, hs;
	ProteinReference pr1, pr2;
	Gene g1;
	PathwayStep s1;
	Catalysis ca1;
	GeneticInteraction gi1;
	
	@Before
	public void setUp() throws Exception {
		// TODO create/refresh test elements that make sense for the following tests only...
		model = BioPAXLevel.L3.getDefaultFactory().createModel();
		pro1 = model.addNew(Provenance.class, "urn:miriam:pid.pathway");
		pro2 = model.addNew(Provenance.class, "urn:miriam:signaling-gateway");
		pw1 = model.addNew(Pathway.class, "pathway");
		pw2 = model.addNew(Pathway.class, "sub_pathway"); // no 'organism' will be set
		p1 = model.addNew(Protein.class, "p1"); // no dataSource
		conv1 = model.addNew(Conversion.class, "conv1"); // no dataSource
		mm = model.addNew(BioSource.class, "mouse");
		pr1 = model.addNew(ProteinReference.class, "pr1"); 
		p2 = model.addNew(Protein.class, "p2"); 
		pr2 = model.addNew(ProteinReference.class, "pr2"); 
		hs = model.addNew(BioSource.class, "human");
		g1 = model.addNew(Gene.class, "gene1"); // organism is not to set
		s1 = model.addNew(PathwayStep.class, "step1");
		ca1 = model.addNew(Catalysis.class, "catalysis1");
		gi1 = model.addNew(GeneticInteraction.class, "gi1");
		
		pw1.addDataSource(pro1);
		pw1.setStandardName("Pathway");
		pw2.setStandardName("Sub-Pathway");
		pw2.addDataSource(pro2);
		pw1.addPathwayComponent(pw2);
		pw1.addPathwayComponent(conv1);
		conv1.addLeft(p1);
		pw1.setOrganism(mm); // set organism - mouse
		p1.setEntityReference(pr1);
		p2.setEntityReference(pr2);
		pr2.setOrganism(hs);
		pw1.addPathwayComponent(conv1);
		pw1.setStandardName("Pathway1");
		ca1.addControlled(conv1);
		ca1.addController(p2);
		pw1.addPathwayComponent(gi1);
		pw1.addPathwayComponent(ca1);
		gi1.addParticipant(g1);
		pw1.addPathwayOrder(s1);
		s1.addStepProcess(ca1);
		s1.addStepProcess(gi1);		
	}

	
	@Test
	public final void testRun_Domains() {	
		PropertyReasoner prr = new PropertyReasoner("organism", SimpleEditorMap.L3);
		prr.setDomains(Gene.class, Pathway.class); // update for only these types (ignore SequenceEntityReference)
		prr.inferPropertyValue(pw1);
		
		assertEquals(hs, pr2.getOrganism()); // still human, because it was ignored, and wasn't empty!
		assertEquals(0, pr2.getComment().size()); // no comment ;) - i.e., reasoner did not add any 
		
		assertEquals(mm, g1.getOrganism()); // inferred from the parent pathway!
		assertEquals(1, g1.getComment().size()); // a new comment was generated!
		
		assertNull(pr1.getOrganism()); // because ERs were filtered!
		assertEquals(0, pr1.getComment().size()); // no comment ;) - i.e., reasoner did not add any 
	}
	
	
	@Test
	public final void testRun_Basic() {	
		// singular object property
		PropertyReasoner prr = new PropertyReasoner("organism", SimpleEditorMap.L3);
		prr.inferPropertyValue(pw1);
		
		assertEquals(hs, pr2.getOrganism()); // still human, because it wasn't empty!
		assertEquals(0, pr2.getComment().size()); // sure, reasoner did not add a comment 
		assertEquals(mm, pr1.getOrganism());
		assertEquals(mm, g1.getOrganism()); // inferred from the parent pathway!
		assertEquals(1, g1.getComment().size()); // a new comment was generated!
		
		// TODO singular primitive property
		prr.setPropertyName("ph");
		
		// multiple cardinality object property
		prr.setPropertyName("dataSource");
		assertTrue(p1.getDataSource().isEmpty());
		assertEquals(1, pw1.getDataSource().size());
		prr.inferPropertyValue(pw1);
		assertEquals(1, p1.getDataSource().size());
		assertEquals(2, pw2.getDataSource().size());
		//TODO add more checks
		
		// TODO multiple primitive property
		// Note: in practice, it's not a good idea to infer names though ;-)
		prr.setPropertyName("name");
		
	}

	
	@Test
	public final void testRun_Clear() {
		PropertyReasoner prr = new PropertyReasoner(null,SimpleEditorMap.L3);
		prr.setPropertyName("organism");
		prr.clearProperty(pw1);
		
		//printModel();
		
		assertNull(pr2.getOrganism());
		assertEquals(2, pr2.getComment().size());
		assertNull(pr1.getOrganism());
		assertEquals(1, pr1.getComment().size());
		assertNull(g1.getOrganism()); 
		assertEquals(1, g1.getComment().size());
	}

	
	@Test
	public final void testRun_AutoOverride() {
		// single cardinality
		PropertyReasoner prr = new PropertyReasoner(null, SimpleEditorMap.L3);
		prr.setPropertyName("organism");
		
		assertNull(pr1.getOrganism());
		assertEquals(mm, pw1.getOrganism());
		
		prr.setGenerateComments(false);
		prr.inferPropertyValue(pw1, hs); // use optional default value

		// default 'hs' was ignored because pw1 had 'mm' already
		assertEquals(mm, pr1.getOrganism());
		assertEquals(0, pr1.getComment().size()); // comments are off
		assertEquals(mm, pw2.getOrganism());
		assertEquals(0, pw2.getComment().size()); // comments are off
		assertEquals(hs, pr2.getOrganism()); // it was 'hs' before - unchanged
		
		// clear/change property and run one more time -
		prr.clearProperty(pw1);
		pr2.setOrganism(mm);
		
		prr.inferPropertyValue(pw1, hs); // use default value
		
		//printModel();

		assertEquals(hs, pw1.getOrganism());
		assertEquals(hs, pr1.getOrganism());
		assertEquals(hs, pw2.getOrganism());
		assertEquals(mm, pr2.getOrganism()); // unchanged (wasn't empty)
	}
	
	
	@Test
	public final void testRun_AutoOverride1() {
		// single cardinality
		PropertyReasoner prr = new PropertyReasoner(null, SimpleEditorMap.L3);
		prr.setPropertyName("organism");
		
		assertNull(pr1.getOrganism());
		
		prr.inferPropertyValue(conv1, hs); // use optional default value
		
		//printModel();

		assertEquals(hs, pr1.getOrganism());
		assertEquals(1, pr1.getComment().size());
	}
	
	
	@Test
	public final void testRun_AutoOverride2() {
		// single cardinality
		PropertyReasoner prr = new PropertyReasoner(null, SimpleEditorMap.L3);
		prr.setPropertyName("organism");
		
		assertNull(pr1.getOrganism());
		
		prr.inferPropertyValue(conv1, null); // no default value - same as prr.inferPropertyValue(conv1)
		
		assertNull(pr1.getOrganism()); // no changes
	}
	

	@Test
	public final void testRun_DefaultOverride() {
		// single cardinality
		PropertyReasoner prr = new PropertyReasoner(null, SimpleEditorMap.L3);
		prr.setPropertyName("organism");
		
		assertEquals(hs, pr2.getOrganism());
		assertNull(pr1.getOrganism());
		assertNull(g1.getOrganism()); 
		
		prr.resetPropertyValue(pw1, mm);
		
		//printModel();
		
		assertEquals(0, pw1.getComment().size()); // was already 'mouse'
		assertEquals(mm, pw2.getOrganism());
		assertEquals(1, pw2.getComment().size()); //added
		assertEquals(mm, pr2.getOrganism());
		assertEquals(2, pr2.getComment().size()); 
		assertEquals(mm, pr1.getOrganism());
		assertEquals(1, pr1.getComment().size());
		assertEquals(mm, g1.getOrganism()); 
		assertEquals(1, g1.getComment().size());
		
		// TODO check for a multiple cardinality prop (dataSource, name)
	}

	
	@Test
	public final void testRun_NullOverride() {
		// single cardinality
		PropertyReasoner prr = new PropertyReasoner(null, SimpleEditorMap.L3);
		prr.setPropertyName("organism");
		try {
			prr.resetPropertyValue(pw1, null);
			fail("Must fail!");
		} catch (Exception e) {
		}
	}
	
	
	void printModel() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		new SimpleIOHandler().convertToOWL(model, bytes);
		System.out.println(bytes.toString());
	}
}
