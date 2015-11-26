package org.biopax.paxtools.normalizer;

/*
 * #%L
 * BioPAX Validator
 * %%
 * Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * 
 * @author rodche
 *
 */
public class NormalizerTest {
	
	static SimpleIOHandler simpleIO;
	
	static {
		simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
		simpleIO.mergeDuplicates(true);
	}

	@Test
	public final void testUri() {
		 // using null or non-standard db
		 assertEquals(Normalizer.uri("test/", "foo", "bar", UnificationXref.class), Normalizer.uri("test/", "FOo", "bar", UnificationXref.class));
		 //'pubchem' is a ambigous synonym (correct ones are: pubchem-substance, pubchem-compound, etc.)
		 assertEquals(Normalizer.uri("", "pubchem", "bar", UnificationXref.class), Normalizer.uri("", "PubChem", "bar", UnificationXref.class));
		 assertEquals(Normalizer.uri("", null, "bar", UnificationXref.class), Normalizer.uri(null, null, "bar", UnificationXref.class));
		 assertFalse(Normalizer.uri(null, "foo", "bar", UnificationXref.class).equals(Normalizer.uri(null, "foo", "BAR", UnificationXref.class)));
		 assertFalse(Normalizer.uri(null, "foo", "bar", UnificationXref.class).equals(Normalizer.uri(null, "foo", "bar", PublicationXref.class)));
		 
		 // using standard db names (Miriam is used to normalize name and/or get identifiers.org URI) -
		 assertEquals(Normalizer.uri("test/", "pubmed", "12345", PublicationXref.class), Normalizer.uri("test/", "PubMED", "12345", PublicationXref.class));
		 assertEquals("http://identifiers.org/pubmed/12345", Normalizer.uri("test/", "PubMED", "12345", PublicationXref.class));
		 assertFalse("http://identifiers.org/pubmed/12345".equals(Normalizer.uri(null, "PubMED", "12345", RelationshipXref.class))); //- not PublicationXref
		 
		 assertEquals("http://identifiers.org/chebi/CHEBI:12345",Normalizer.uri("", "chebi", "CHEBI:12345", SmallMoleculeReference.class));
		 assertEquals("http://identifiers.org/pubchem.substance/12345",Normalizer.uri("", "pubchem-substance", "12345", SmallMoleculeReference.class));
		 
		 System.setProperty("biopax.normalizer.uri.strategy", Normalizer.VALUE_NORMALIZER_URI_STRATEGY_SIMPLE);
		 assertEquals("SequenceModificationVocabulary_protein_modification_ontology_MOD_12345",Normalizer.uri("", "PSI-mod", "MOD:12345", SequenceModificationVocabulary.class));
		 assertEquals("ControlledVocabulary_protein_modification_ontology_MOD_12345",Normalizer.uri("", "MOD", "MOD:12345", ControlledVocabulary.class));
		 System.setProperty("biopax.normalizer.uri.strategy", Normalizer.VALUE_NORMALIZER_URI_STRATEGY_MD5);
		 //wrong id (case-sens.)
		 assertFalse("http://identifiers.org/chebi/CHEBI:12345".equals(Normalizer.uri("", "chebi", "chebi:12345", SmallMoleculeReference.class)));
		 //no 'pubchem' namespace there
		 assertFalse("http://identifiers.org/pubchem/12345".equals(Normalizer.uri("", "pubchem-substance", "12345", UnificationXref.class)));
		 
		 //when there're special symbols, spaces in the 'id' part
		 assertEquals("UnificationXref_foo_bar", Normalizer.uri(null, null, "foo bar", UnificationXref.class));
	}
	
	
	@Test
	public final void testNormalize() throws UnsupportedEncodingException {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
    	Xref ref = model.addNew(UnificationXref.class, "Xref1");
    	ref.setDb("uniprotkb");
    	ref.setId("P68250");
    	ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference1");
    	pr.setDisplayName("ProteinReference1");
    	pr.addXref(ref);
    	ref = model.addNew(RelationshipXref.class, "Xref2");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
    	ref.setIdVersion("1");  // this xref won't be removed by norm. (version matters in xrefs comparing!)
		pr.addXref(ref);
	   	ref = model.addNew(UnificationXref.class, "Xref3");
    	ref.setDb("uniprotkb"); // will be converted to 'uniprot'
    	/* The following ID is the secondary accession of P68250, 
    	 * but Normalizer won't complain (it's Validator's and - later - Merger's job)!
    	 * However, if it were P68250, the normalize(model) would throw exception
    	 * (because ProteinReference1 becomes ProteinReference2, both get RDFId= urn:miriam:uniprot:P68250!)
    	 */
    	ref.setId("Q0VCL1"); 
    	Xref uniprotX = ref;
    	
    	pr = model.addNew(ProteinReference.class, "ProteinReference2");
    	pr.setDisplayName("ProteinReference2");
    	pr.addXref(uniprotX);
    	ref = model.addNew(RelationshipXref.class, "Xref4");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
		pr.addXref(ref);
		
		// this ER is duplicate (same uniprot xref as ProteinReference2's) and must be removed by normalizer
    	pr = model.addNew(ProteinReference.class, "ProteinReference3");
    	pr.setDisplayName("ProteinReference3");
    	pr.addXref(uniprotX);
    	ref = model.addNew(RelationshipXref.class, "Xref5");
    	ref.setDb("refseq");
    	ref.setId("NP_001734");
		pr.addXref(ref);
		
		// normalizer won't merge diff. types of xref with the same db:id
	   	ref = model.addNew(PublicationXref.class, "Xref6");
    	ref.setDb("pubmed");
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);
	   	ref = model.addNew(RelationshipXref.class,"Xref7");
    	ref.setDb("pubmed"); 
    	ref.setId("2549346"); // the same id
    	pr.addXref(ref);

		// add biosource
	   	ref = model.addNew(UnificationXref.class, "Xref8");
    	ref.setDb("taxonomy"); 
    	ref.setId("10090"); // the same id
		BioSource bioSource = model.addNew(BioSource.class, "BioSource_Mouse_Tissue");
		bioSource.addXref((UnificationXref)ref);

		// Provenance (must set ID and standard names from a name)
		Provenance pro1 = model.addNew(Provenance.class, "pid");
		pro1.addName("nci_nature"); // must be case insensitive (recognized)
		pro1.setStandardName("foo"); // must be replaced
		// Provenance (must create names from urn)
		Provenance pro2 = model.addNew(Provenance.class, "http://identifiers.org/signaling-gateway/");
		
		// add some entities with props
		Pathway pw1 = model.addNew(Pathway.class, "pathway");
		pw1.addDataSource(pro1);
		pw1.setStandardName("Pathway");
		Pathway pw2 = model.addNew(Pathway.class, "sub_pathway");
		pw2.setStandardName("Sub-Pathway");
		pw2.addDataSource(pro2);
		pw1.addPathwayComponent(pw2);
		
		// add data to test uniprot isoform xref and PR normalization
    	ref = model.addNew(UnificationXref.class, "Xref9");
    	ref.setDb("UniProt"); // normalizer will detect/change to "UniProt Isoform"
    	ref.setId("P68250-2");
    	pr = model.addNew(ProteinReference.class, "ProteinReference4");
    	pr.setDisplayName("ProteinReference1isoformA");
    	pr.addXref(ref);
    	
    	// next ones are to test normalizer can auto-fix 'uniprot' to 'uniprot isoform' xref, 
    	// and also merge xrefs #9,#10 and PRs #4,#5 into one PR with one xref
    	//below, uniprot xref's idVersion='2' will be moved back to the id value, and db set to "UniProt Isoform" -
    	ref = model.addNew(UnificationXref.class, "Xref10");
    	ref.setDb("UniProtKb");
    	ref.setId("P68250");
    	ref.setIdVersion("2");
    	pr = model.addNew(ProteinReference.class, "ProteinReference5");
    	pr.setDisplayName("ProteinReference1isoformB");
    	pr.addXref(ref);   	
    	
		// All following three Xrefs and PRs must be normalized to uniprot.isoform:P68250-1 and merged into one!
    	ref = model.addNew(UnificationXref.class, "Xref11");
    	ref.setDb("UniProt Isoform");
    	ref.setId("P68250-1"); //- same as the canonical P68250
    	pr = model.addNew(ProteinReference.class, "ProteinReference6");
    	pr.addXref(ref);
    	ref = model.addNew(UnificationXref.class, "Xref12");
    	ref.setDb("UniProt");
    	ref.setId("P68250");
    	ref.setIdVersion("1");
    	pr = model.addNew(ProteinReference.class, "ProteinReference7");
    	pr.addXref(ref);   	
    	ref = model.addNew(UnificationXref.class, "Xref13");
    	ref.setDb("UniProt Isoform");
    	ref.setId("P68250-1");
    	pr = model.addNew(ProteinReference.class, "ProteinReference8");
    	pr.addXref(ref);
    	
		//model.setXmlBase(null); //default
    	
		// go normalize!	
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model); 
		
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		simpleIO.convertToOWL(model, out);
//		System.out.println(out.toString());
		
		// check Xref
		String normUri = Normalizer.uri(model.getXmlBase(), "uniprot", "P68250", UnificationXref.class);
		BioPAXElement bpe = model.getByID(normUri);
		assertTrue(bpe instanceof UnificationXref);
		
		// check PR
		bpe = model.getByID("http://identifiers.org/uniprot/Q0VCL1");
		assertTrue(bpe instanceof ProteinReference);
		assertTrue(model.containsID("Xref7")); //RX is not normalized unless (by mistake) it has identifiers.org uri
		
		//test BioSource
		assertFalse(model.containsID("BioSource_Mouse_Tissue"));
		bpe = model.getByID(Normalizer.uri(model.getXmlBase(), "taxonomy", "10090", BioSource.class)); //"taxonomy" - capitalization can be any
		assertTrue(bpe instanceof BioSource);
		normUri = Normalizer.uri(model.getXmlBase(), "taxonomy", "10090", UnificationXref.class);
		bpe = model.getByID(normUri);
		assertTrue(bpe instanceof UnificationXref);
		
		// test that one of each pair ProteinReference, 2nd,3rd and 4th,5th is removed/merged:
		assertEquals(4, model.getObjects(ProteinReference.class).size());
		
		// Provenance is no more normalized (Miriam is not enough for this task)!
		assertEquals(2, model.getObjects(Provenance.class).size());
		
		// dataSource property is not inferred/inherited from pw1 anymore (prop. inference feature was removed)
		pw2 = (Pathway) model.getByID("sub_pathway");
		assertEquals(1, pw2.getDataSource().size());
		pw1 = (Pathway) model.getByID("pathway");
		assertEquals(1, pw1.getDataSource().size());
		
		//test uniprot isoform xrefs are detected and normalized the same way
		// get the expected xref URI first
		normUri = Normalizer.uri(model.getXmlBase(), "uniprot isoform", "P68250-2", UnificationXref.class);
		bpe = model.getByID(normUri);
		assertNotNull(bpe);
		assertEquals(1, ((Xref)bpe).getXrefOf().size());
	}

	
	@Test
	public final void testAutoName() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Provenance pro = model.addNew(Provenance.class, "http://identifiers.org/pid.pathway/");
		pro.setStandardName("foo");
		Normalizer.autoName(pro);
		assertNotNull(pro.getStandardName());
		assertTrue(pro.getName().contains("PID"));
		assertTrue(pro.getName().contains("NCI_Nature curated"));
		assertFalse(pro.getStandardName().equals("foo"));
	}
	
	
	@Test
	public final void testNormalize2() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
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
		
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model);
		
		ProteinReference e = (ProteinReference) model.getByID("http://identifiers.org/uniprot/Q0VCL1");
		assertNotNull(e);
		
		assertEquals(4, e.getXref().size());
		print(e, model);
	}
	
	@Test
	public final void testNormalize3() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Xref ref =  model.addNew(UnificationXref.class, "Xref1");
    	ref.setDb("uniprotkb"); // will be converted to 'uniprot'
    	ref.setId("Q0VCL1"); 
    	ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference1");
    	pr.setDisplayName("A ProteinReference");
    	pr.addXref(ref);
    	assertEquals(1, ref.getXrefOf().size());
    	
    	System.out.println("Before the model is normalized - ");
    	print(pr, model);
		
    	// go normalize!
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model);
		
		System.out.println("After the model is normalized - ");
		print(pr, model);
		
		assertFalse(model.contains(pr)); // replaced by new norm. PR in the model
		assertFalse(model.contains(ref)); // replaced by new norm. xref in the model
		//now xrefOf is consistent with xref for all objects inn the model (since after some paxtools 4.1.3 snapshot)
		assertEquals(0, pr.getXref().size()); // old PR has xref removed!
		assertEquals(0, ref.getXrefOf().size()); // because the old xref was replaced in all parent elements!
		
		ProteinReference e = (ProteinReference) model.getByID("http://identifiers.org/uniprot/Q0VCL1");
		assertNotNull(e);	
		assertEquals(1, e.getXref().size());
		
		String normUri = Normalizer.uri(model.getXmlBase(), "UNIPROT", "Q0VCL1", UnificationXref.class);
		ref = (UnificationXref) model.getByID(normUri);
		assertNotNull(ref);
		assertEquals(1, ref.getXrefOf().size());
		
		print(e, model);
	}
	
	
	@Test
	public final void testNormalizeInoh() {
		//there're two Protein objects that have entityReference 
		//rdf:ID="IMR_0100366_G_alpha_s_Canonical" (in fact a generic ProteinReference inproperly defined);
		//and that PR has two UniProt UnificationXref: P63092 (human), P63095 (rat).
		//Identifiers.org URI for the PR should NOT be generated.
		//
		//Also, there was a bug when replacing the PR's URI (warning: IllegalBioPAXArgumentException: Incompatible type!..)
		//The cause is that there was a (weird, invalid) PublicationXref having db:id as UniProt:P63092 that gets the same URI!
		//To avoid such issues altogether (despite illegal pub. xrefs), the Normalizer won't use Identifiers.org for PublicationXrefs anymore.
		
		Model model = simpleIO.convertFromOWL(getClass().getResourceAsStream("/test-inoh.owl"));
		Normalizer normalizer = new Normalizer();
		normalizer.setXmlBase("");
		normalizer.normalize(model);

		//A weird PublicationXref that uses a UniProt ID won't be normalized:
		assertFalse(model.containsID("http://identifiers.org/uniprot/P63092"));

		//A PR with two UniProt IDs/unif.xrefs - human, rat - won't be normalized!
		assertTrue(model.containsID(model.getXmlBase() + "IMR_0100366_G_alpha_s_Canonical"));
		assertEquals("ProteinReference", model.getByID(model.getXmlBase() + "IMR_0100366_G_alpha_s_Canonical")
				.getModelInterface().getSimpleName());
	}
	
	private void print(XReferrable xr, Model m) {
		System.out.println("model=" + m.contains(xr) + ":\t" 
			+ xr.getUri() +
			" is " + xr.getModelInterface().getSimpleName()
			+ " and has xrefs: ");
		for(Xref x : xr.getXref()) {
			System.out.println("model=" + m.contains(x) + ":\t" 
				+"  " + x + " is " 
				+ x.getModelInterface().getSimpleName() 
				+ " - " + x.getUri() + ", db=" + x.getDb()
				+ ", id=" + x.getId() + ", idVer=" + x.getIdVersion());
			for(XReferrable rx : x.getXrefOf()) {
				System.out.println("model=" + m.contains(rx) + ":\t" 
					+ "    xrefOf: " + rx);
			}
		}
	}
}
