package org.biopax.paxtools.normalizer;


import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author rodche
 */
public class NormalizerTest {

	static SimpleIOHandler simpleIO;

	static {
		simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
		simpleIO.mergeDuplicates(true);
	}

	@ParameterizedTest
	@MethodSource
	void uri(String expected, String xmlBase, String db, String id, Class<? extends BioPAXElement> type) {
		assertEquals(expected, Normalizer.uri(xmlBase, db, id, type));
	}

	// args source for the above parameterized tests
	private static Stream<Arguments> uri() {
		return Stream.of(
				arguments("SMV_mod_MOD_12345", "", "mod", "MOD:12345", SequenceModificationVocabulary.class),
				arguments("V_mod_MOD_12345", "", "MOD", "MOD:12345", ControlledVocabulary.class),
				arguments("test/UX_foo_bar", "test/", "FOo", "bar", UnificationXref.class),//non-standard db
				arguments("test/UX_foo_bar", "test/", "foo", "bar", UnificationXref.class),
				//'pubchem' is ambiguous (correct: pubchem-substance, pubchem-compound)
				arguments("UX_pubchem_bar", "", "pubchem", "bar", UnificationXref.class),
				arguments("UX_pubchem_bar", "", "PubChem", "bar", UnificationXref.class),
				arguments("UX_bar", "", null, "bar", UnificationXref.class),//null db
				arguments("UX_bar", null, null, "bar", UnificationXref.class),
				//different ID (case) or type makes different URI
				arguments("UX_foo_BAR", null, "foo", "BAR", UnificationXref.class),
				arguments("UX_foo_bar", null, "foo", "bar", UnificationXref.class),
				arguments("PX_foo_bar", null, "foo", "bar", PublicationXref.class),
				arguments("PX_foo_BAR", null, "foo", "BAR", PublicationXref.class),
				arguments("pubmed:12345", "test/", "PubMED", "12345", PublicationXref.class),
				arguments("pubmed:12345", "", "pubmed", "12345", PublicationXref.class),
				arguments("pubmed:12345", "test/", "MEDLINE", "12345", PublicationXref.class),
				arguments("RX_pubmed_12345", null, "PubMED", "12345", RelationshipXref.class), //not PublicationXref
				arguments("http://bioregistry.io/chebi:12345", "", "chebi", "CHEBI:12345", SmallMoleculeReference.class),
				arguments("http://bioregistry.io/pubchem.substance:12345", "", "pubchem-substance", "12345", SmallMoleculeReference.class),
				//invalid chebi id (its "banana", if present, is case-sensitive; i.e., CHEBI:12345 or 12345 would match the pattern)
				arguments("SMR_bd1c4fd7641bf774e58bda352272c720", "", "chebi", "chebi:12345", SmallMoleculeReference.class),
				arguments("http://bioregistry.io/chebi:12345", "test/", "chebi", "12345", SmallMoleculeReference.class),
				arguments("http://bioregistry.io/chebi:12345", "test/", "chebi", "CHEBI:12345", SmallMoleculeReference.class),
				arguments("http://bioregistry.io/pubchem.compound:12345", "", "pubchem", "12345", SmallMoleculeReference.class),
				arguments("http://bioregistry.io/pubchem.substance:12345", "", "pubchem-substance", "12345", SmallMoleculeReference.class),
				//special symbols or spaces in the 'id' part get replaced with underscore in the URI
				arguments("UX_Foo_Bar", null, null, "Foo Bar", UnificationXref.class),
				arguments("UX_Foo_Bar", null, null, "Foo&Bar", UnificationXref.class), //todo: the same expected URI as above can be wrong/bug...
				arguments("uniprot:W0C7J9", "", "UniProt", "W0C7J9", UnificationXref.class),
				arguments("http://bioregistry.io/ncbitaxon:9606", null, "taxonomy", "9606", BioSource.class),
				arguments("http://bioregistry.io/ncbitaxon:9606", null, "NCBI Taxonomy", "9606", BioSource.class),
				arguments("http://bioregistry.io/ncbitaxon:9606", null, "NEWT", "9606", BioSource.class),
				//when organism's id is not taxID (e.g., if the BioSource has tissue, cellType CVs...)
				arguments("BIO_taxonomy_9606_blah_blah", null, "taxonomy", "9606_blah_blah", BioSource.class)
		);
	}


	@Test
	void normalize() {
		// Note: a UniProt AC version (e.g. P68250.1 .. P68250.94) is not the same thing as isoform ID!
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
		/* Q0VCL1 is the secondary accession for P68250, but Normalizer won't mind nor merge these.
		 * If it were P68250 here, normalize(model) would throw exception because ProteinReference1
		 * becomes ProteinReference2, both getting the same uniprot:P68250 unification xref.
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
		bioSource.addXref(ref);

		// Provenance
		Provenance pro1 = model.addNew(Provenance.class, "pid");
		pro1.addName("nci_nature"); // must be case-insensitive (recognized)
		pro1.setStandardName("foo"); // must be replaced
		// Provenance (it won't create names from the urn unless we call: Normalizer.autoName(pro2);)
		Provenance pro2 = model.addNew(Provenance.class, "http://bioregistry.io/signaling-gateway/");

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
		ref.setDb("UniProt"); // normalizer will change it to "uniprot isoform"
		ref.setId("P68250-2");
		pr = model.addNew(ProteinReference.class, "ProteinReference4");
		pr.setDisplayName("ProteinReference1isoformA");
		pr.addXref(ref);

		// next ones are to test normalizer can auto-fix 'uniprot' to 'uniprot isoform' xref,
		// and also merge xrefs #9,#10 and PRs #4,#5 into one PR with one xref
		//below, uniprot xref's idVersion='2' will be moved back to the id value, and db set to "UniProt Isoform" -
		ref = model.addNew(UnificationXref.class, "Xref10");
		ref.setDb("UniProtKb"); // NOT to be replaced with "UniProt Isoform" (version and isoform # are not the same thing)
		ref.setId("P68250");
		ref.setIdVersion("2"); //may be lost after merging with two other P68250 xrefs
		// (version is not the same as isoform, unless db name is 'uniprot isoform')
		pr = model.addNew(ProteinReference.class, "ProteinReference5");
		pr.setDisplayName("ProteinReference1isoformB");
		pr.addXref(ref);

		// Following three Xrefs and PRs will be normalized to uniprot.isoform:P68250-1 and merged into one
		ref = model.addNew(UnificationXref.class, "Xref11");
		ref.setDb("UniProtKb"); // will be replaced with "uniprot isoform"
		ref.setId("P68250-1");
		pr = model.addNew(ProteinReference.class, "ProteinReference6");
		pr.addXref(ref);
		ref = model.addNew(UnificationXref.class, "Xref12");
		ref.setDb("UniProt Isoform"); // because this is standard (isoform) db name (special case) ->
		ref.setId("P68250"); //- this id will set to "P68250-1",
		ref.setIdVersion("1"); //- and idVersion will be cleared!
		pr = model.addNew(ProteinReference.class, "ProteinReference7");
		pr.addXref(ref);
		ref = model.addNew(UnificationXref.class, "Xref13");
		ref.setDb("UniProt Isoform");
		ref.setId("P68250-1");
		pr = model.addNew(ProteinReference.class, "ProteinReference8");
		pr.addXref(ref);

		//special dangling UXs to test/catch a weird bug that accidentally makes db='uniprot isoform'...
		UnificationXref ux = model.addNew(UnificationXref.class, "UniprotUX1");
		ux.setDb("uniprot");
		ux.setId("W0C7J9");
		ux = model.addNew(UnificationXref.class, "UniprotUX2");
		ux.setDb("uniprot");
		ux.setId("W0C7J9.1"); //NOT to be changed to 'uniprot isoform'
		ux = model.addNew(UnificationXref.class, "UniprotUX3");
		ux.setDb("uniprot");
		ux.setId("W0C7J9"); //NOT to be changed to 'uniprot isoform'
		ux.setIdVersion("1");
		ux = model.addNew(UnificationXref.class, "UniprotUX4");
		ux.setDb("uniprot"); //will be changed to 'uniprot isoform'
		ux.setId("W0C7J9-1");
		ux = model.addNew(UnificationXref.class, "UniprotUX5");
		ux.setDb("uniprot"); //will be changed to 'uniprot isoform'
		ux.setId("P68250-3");

		// go normalize!
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model);

		// test for a bug that causes db='uniprot' become 'uniprot isoform' (the id matches both patterns)
		assertTrue(model.containsID("uniprot:W0C7J9"));
		assertFalse(model.containsID("uniprot.isoform:W0C7J9")); //it's auto fixed/replaced with uniprot:W0C7J9
		assertTrue(model.containsID("uniprot.isoform:W0C7J9-1"));
		assertTrue(model.containsID("uniprot.isoform:P68250-3"));
		assertTrue(model.containsID("ncbitaxon:10090"));
		assertTrue(model.containsID("http://bioregistry.io/ncbitaxon:10090")); //was "taxonomy"

		// check Xref
		String normUri = Normalizer.uri(model.getXmlBase(), "uniprot", "P68250", UnificationXref.class);
		BioPAXElement bpe = model.getByID(normUri);
		assertTrue(bpe instanceof UnificationXref);

		// check PR
		bpe = model.getByID("http://bioregistry.io/uniprot:Q0VCL1");
		assertTrue(bpe instanceof ProteinReference);
		assertFalse(model.containsID("Xref7")); //RXs are now all normalized as well

		//test BioSource
		assertFalse(model.containsID("BioSource_Mouse_Tissue"));
		bpe = model.getByID(Normalizer.uri(model.getXmlBase(), "taxonomy", "10090", BioSource.class));
		assertTrue(bpe instanceof BioSource);
		normUri = Normalizer.uri(model.getXmlBase(), "taxonomy", "10090", UnificationXref.class);
		assertEquals("ncbitaxon:10090", normUri);
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
	void autoName() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Provenance pro = model.addNew(Provenance.class, "http://bioregistry.io/pid.pathway/");
		pro.setStandardName("foo");
		Normalizer.autoName(pro);
		Provenance pro2 = model.addNew(Provenance.class, "http://bioregistry.io/signaling-gateway"); // ending / doesn't matter
		Normalizer.autoName(pro2);
		assertAll(
				() -> assertTrue(pro.getName().contains("pid.pathway")),
				() -> assertEquals("NCI Pathway Interaction Database: Pathway",pro.getDisplayName()),
				() -> assertEquals("NCI Pathway Interaction Database: Pathway", pro.getStandardName()),
				() -> assertEquals("Signaling Gateway",pro2.getStandardName()),
				() -> assertEquals("Signaling Gateway",pro2.getDisplayName()),
				() -> assertTrue(pro2.getName().contains("signaling-gateway"))
		);
	}


	@Test
	void normalize2() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Xref ref =  model.addNew(UnificationXref.class, "Xref1");
		ref.setDb("uniprot");
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

		ProteinReference e = (ProteinReference) model.getByID("http://bioregistry.io/uniprot:Q0VCL1");
		assertNotNull(e);
		assertEquals(4, e.getXref().size());
	}

	@Test
	void normalize3() {
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Xref ref =  model.addNew(UnificationXref.class, "Xref1");
		ref.setDb("uniprot"); // will be converted to 'uniprot'
		ref.setId("Q0VCL1");
		ProteinReference pr = model.addNew(ProteinReference.class, "ProteinReference1");
		pr.setDisplayName("A ProteinReference");
		pr.addXref(ref);
		assertEquals(1, ref.getXrefOf().size());

		// go normalize!
		Normalizer normalizer = new Normalizer();
		normalizer.normalize(model);

		assertFalse(model.contains(pr)); // replaced by new norm. PR in the model
		assertFalse(model.contains(ref)); // replaced by new norm. xref in the model
		//now xrefOf is consistent with xref for all objects inn the model (since after some paxtools 4.1.3 snapshot)
		assertEquals(0, pr.getXref().size()); // old PR has xref removed!
		assertEquals(0, ref.getXrefOf().size()); // because the old xref was replaced in all parent elements!

		ProteinReference e = (ProteinReference) model.getByID("http://bioregistry.io/uniprot:Q0VCL1");
		assertNotNull(e);
		assertEquals(1, e.getXref().size());

		String normUri = Normalizer.uri(model.getXmlBase(), "UNIPROT", "Q0VCL1", UnificationXref.class);
		ref = (UnificationXref) model.getByID(normUri);
		assertNotNull(ref);
		assertEquals(1, ref.getXrefOf().size());
	}


	@Test
	void normalizeInoh() {
		//There are two Proteins that have entityReference (PR) rdf:ID="IMR_0100366_G_alpha_s_Canonical",
		//which has two uniprot unification xrefs: P63092 (human) and P63095 (rat).
		//Therefore, the URI of this PR should NOT be normalized.
		//Also, there was a bug when replacing the PR's URI (warning: IllegalBioPAXArgumentException: Incompatible type!..)
		//as the invalid PublicationXref UniProt:P63092 was getting the same normalized URI as some PR.
		//To avoid all such issues (regardless invalid pub. xrefs), the Normalizer won't make
		//canonical URI for a PublicationXref anymore unless the xref.db is "pubmed" (or synonym).
		Model model = simpleIO.convertFromOWL(getClass().getResourceAsStream("/test-inoh.owl"));
		Normalizer normalizer = new Normalizer();
		normalizer.setXmlBase("");
		normalizer.normalize(model);

		//All PublicationXref URIs either start with "pubmed:" or "PX_" prefix:
		model.getObjects(PublicationXref.class).stream().forEach(px -> assertTrue(StringUtils.startsWithAny(px.getUri(),"pubmed:","PX_")));

		BioPAXElement e = model.getByID("uniprot:P63092");
		assertTrue(e instanceof UnificationXref);

		//A PR with two different UniProt unif. xrefs - human, rat - won't be normalized!
		e = model.getByID(model.getXmlBase() + "IMR_0100366_G_alpha_s_Canonical");
		assertTrue(e instanceof ProteinReference);
	}

	private void print(XReferrable xr, Model m) {
		System.out.println();
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
