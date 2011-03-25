/**
 * 
 */
package org.biopax.paxtools.controller;

import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author rodche
 *
 */
public class SimpleMergerTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link org.biopax.paxtools.controller.SimpleMerger#merge(org.biopax.paxtools.model.Model, java.util.Collection)}.
	 */
	@Test
	public final void testMergeModelCollectionOfQextendsBioPAXElement() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Model model = factory.createModel();
		Xref ref =  model.addNew(UnificationXref.class, "Xref1");
    	ref.setDb("uniprotkb"); // will be converted to 'uniprot'
    	ref.setId("Q0VCL1"); 
    	Xref uniprotX = ref;
    	ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference");
    	pr.setDisplayName("ProteinReference");
    	pr.addXref(uniprotX);
    	ref = model.addNew(RelationshipXref.class, "Xref2");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
		pr.addXref(ref);
		// normalizer won't merge diff. types of xref with the same db:id
	   	ref = model.addNew(PublicationXref.class, "Xref3");
    	ref.setDb("pubmed");
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);
	   	ref = model.addNew(RelationshipXref.class,"Xref4");
    	ref.setDb("pubmed"); 
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);
    	
    	// create these, add to properties, but not add to the model explicitely 
    	//(merger should find and add them!)
		BioSource bs = factory.create(BioSource.class, "Mouse");
    	ref = factory.create(UnificationXref.class, "Xref5");
    	ref.setDb("taxonomy"); 
    	ref.setId("10090"); // the same id
		bs.addXref(ref);
		pr.setOrganism(bs);
		
		assertEquals(5, model.getObjects().size());
		
		SimpleMerger merger = new SimpleMerger(new SimpleEditorMap(BioPAXLevel.L3));
		merger.merge(model); // 
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			(new SimpleExporter(BioPAXLevel.L3)).convertToOWL(model, out);
			System.out.println(out.toString());
		} catch (IOException e) {
			fail(e.toString());
		}
		
		assertEquals(7, model.getObjects().size()); // Bug fixed: SimpleMerger adds BioSource to the model but not its children (xref)!
		bs = (BioSource) model.getByID("Mouse");
		assertEquals(1, bs.getXref().size());
		pr = (ProteinReference) model.getByID("ProteinReference");
		assertEquals(4, pr.getXref().size());
	}

}
