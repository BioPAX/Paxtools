package org.biopax.paxtools.normalizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResolverTest {
	static final String MI = "Molecular Interactions Controlled Vocabulary"; //name corresponds to "mi" record in bioregistry.io

	@Test
	public final void getNamespace() {
		Assertions.assertAll(
				() ->	Assertions.assertNotNull(Resolver.getNamespace("obo.mi")),//auto-detected as "mi"
				() ->	Assertions.assertEquals(MI, Resolver.getNamespace("urn:miriam:mi").getName()),
				() ->	Assertions.assertNotNull(Resolver.getNamespace("psi-mi")),//becomes "mi"
				() ->	Assertions.assertNotNull(Resolver.getNamespace("MolecularInteractions Ontology", true)),//misspelling variant (allowed by default too)
				() ->	Assertions.assertNull(Resolver.getNamespace("MolecularInteractions Ontology", false)),//null when spelling variants not allowed
				() ->	Assertions.assertNotNull(Resolver.getNamespace("http://bioregistry.io/chebi")),
				() ->	Assertions.assertNotNull(Resolver.getNamespace("bioregistry.io/uniprot")),
				() ->	Assertions.assertNotNull(Resolver.getNamespace("https://identifiers.org/psi-mi")), //matches "mi" registry prefix/namespace
				() ->	Assertions.assertEquals("genpept", Resolver.getNamespace("Entrez Protein Sequence").getPrefix()),
				() ->	Assertions.assertEquals("ncbitaxon", Resolver.getNamespace("NCBI Taxonomy").getPrefix())
		);
	}

	@Test
	public final void getPattern() {
		Assertions.assertEquals("^\\d{4}$", Resolver.getNamespace(MI).getPattern());
	}


	@Test
	public final void checkRegExp() {
		Assertions.assertAll(
				() -> Assertions.assertTrue(Resolver.checkRegExp("MI:0000", MI)), //auto-ignores "MI:"
				() -> Assertions.assertTrue(Resolver.checkRegExp("0000", MI)),
				() -> Assertions.assertFalse(Resolver.checkRegExp("0000", "foo")), //no such collection
				() -> Assertions.assertTrue(Resolver.checkRegExp("0000", "aaindex")),//aaindex no pattern
				() -> Assertions.assertTrue(Resolver.checkRegExp("98346", "bind"))//no pattern
		);
	}

	@Test
	public final void getURI() {
		Assertions.assertAll(
				//standard prefix and id with "banana" prefix
				() -> Assertions.assertEquals("http://bioregistry.io/chebi:36927", Resolver.getURI("chebi", "CHEBI:36927")),
				//old ns prefix (should auto-fix as "chebi") and id without "CHEBI:" banana...
				() -> Assertions.assertEquals("http://bioregistry.io/chebi:36927", Resolver.getURI("identifiers.org/chebi/", "36927")),
				//similar, but using bioregistry.io/ base, with ending slash, and with banana...
				() -> Assertions.assertEquals("http://bioregistry.io/chebi:36927", Resolver.getURI("bioregistry.io/chebi", "CHEBI:36927")),
				() -> Assertions.assertEquals("http://bioregistry.io/chebi:36927", Resolver.getURI("http://bioregistry.io/chebi", "CHEBI:36927")),
				() -> Assertions.assertEquals("http://bioregistry.io/chebi:36927", Resolver.getURI("https://bioregistry.io/chebi", "CHEBI:36927")),
				() -> Assertions.assertEquals("http://bioregistry.io/pubchem.compound:1", Resolver.getURI("CID", "1")),
				() -> Assertions.assertEquals("http://bioregistry.io/pubchem.substance:1", Resolver.getURI("SID", "1")),
				() -> Assertions.assertNull(Resolver.getURI("compound", "1")) //id pattern mismatch (kegg.compound)
		);
	}

	@Test
	public final void getCURIE() {
		Assertions.assertAll(
				() -> Assertions.assertEquals("uniprot:P12345", Resolver.getCURIE("uniprot", "P12345")),
				() -> Assertions.assertEquals("go:0045202", Resolver.getCURIE("go", "GO:0045202"))
		);
	}

}
