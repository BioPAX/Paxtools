/**
 * 
 */
package org.biopax.paxtools.controller;

import org.biopax.paxtools.io.*;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.Filter;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

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

	@Test
	public final void testMergeModel() {
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
		
		// do merge
		SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3);
		merger.merge(model, model); // to itself
		// - can use model.merge(model) or model.repair() instead
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			(new SimpleIOHandler(BioPAXLevel.L3)).convertToOWL(model, out);
			//System.out.println(out.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
		
		bs = (BioSource) model.getByID("Mouse");
		assertEquals(1, bs.getXref().size());
		pr = (ProteinReference) model.getByID("ProteinReference");
		assertEquals(4, pr.getXref().size());
		
		assertEquals(7, model.getObjects().size()); // Bug fixed: SimpleMerger adds BioSource to the model but not its children (xref)!
	}
	
	@Test
	public final void testMergeOneObject() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Model model = factory.createModel();
 	
		Xref ref = factory.create(UnificationXref.class, "ref");
    	ref.setDb("Taxonomy"); 
    	ref.setId("9606"); // the same id
		
		SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3);
		merger.merge(model, ref); 
		
		assertEquals(1, model.getObjects().size());
		
		model = factory.createModel();
    	BioSource bs = factory.create(BioSource.class, "Mouse");
    	bs.addXref(ref);
    	merger.merge(model, bs); 
    	
    	assertEquals(2, model.getObjects().size());
	}
	
	@Test
	public final void testMergeWithFilter() {
		BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
		Model model = factory.createModel();

    	//this one is originally in the (target) model
    	ProteinReference pr = model.addNew(ProteinReference.class, "pr");
    	pr.setDisplayName("one");
    	
    	//the one with the same URI to merge into target
    	ProteinReference pr1 = factory.create(ProteinReference.class, "pr");
		Xref ux = factory.create(UnificationXref.class, "ux");
    	ux.setDb("UniProt"); 
    	ux.setId("P12345"); 
    	pr1.addName("one");
    	pr1.addName("two");
    	pr1.addXref(ux);
    	
    	
    	//merge w/o using filter (to force copying mul. cadr. properties of same-uri objects)
    	SimpleMerger merger = new SimpleMerger(SimpleEditorMap.L3);
    	merger.merge(model, pr1); 
    	
    	assertEquals(2, model.getObjects().size());
    	ProteinReference mergedPr = (ProteinReference) model.getByID("pr");
    	assertTrue(mergedPr.getXref().isEmpty()); //xref wasn't copied
    	assertEquals("one", mergedPr.getDisplayName());
    	assertEquals(1, mergedPr.getName().size()); //"two" wasn't copied
    	
    	// with a filter
    	merger = new SimpleMerger(SimpleEditorMap.L3, new Filter<BioPAXElement>() {		
			public boolean filter(BioPAXElement object) {
				return object instanceof EntityReference;
			}
		});
    	model = factory.createModel();
    	model.add(pr);
    	
    	merger.merge(model, pr1); 
    	
    	assertEquals(2, model.getObjects().size());
    	mergedPr = (ProteinReference) model.getByID("pr");
    	assertFalse(mergedPr.getXref().isEmpty());
    	assertEquals("one", mergedPr.getDisplayName());
    	assertEquals(2,mergedPr.getName().size());
	}

}
