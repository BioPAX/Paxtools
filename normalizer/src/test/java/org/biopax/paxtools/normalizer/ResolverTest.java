package org.biopax.paxtools.normalizer;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResolverTest {
	static final String MI = "Molecular Interactions Controlled Vocabulary"; //name corresponds to "mi" record in bioregistry.io

	@Test
	public final void getNamespace() {
		assertNotNull(Resolver.getNamespace("obo.mi"));//auto-detected as "mi"
		assertEquals(MI, Resolver.getNamespace("urn:miriam:mi").getName());
		assertNotNull(Resolver.getNamespace("psi-mi"));//becomes "mi"
		assertNotNull(Resolver.getNamespace("MolecularInteractions Ontology", true));//misspelling variant (allowed by default)
		assertNull(Resolver.getNamespace("MolecularInteractions Ontology", false));//null when spelling variants not allowed
		assertNotNull(Resolver.getNamespace("http://bioregistry.io/chebi"));
		assertNotNull(Resolver.getNamespace("bioregistry.io/uniprot"));
		assertNotNull(Resolver.getNamespace("https://identifiers.org/psi-mi")); //matches "mi" registry prefix/namespace
	}

	@Test
	public final void getPattern() {
		assertEquals("^\\d{4}$", Resolver.getNamespace(MI).getPattern());
	}


	@Test
	public final void checkRegExp() {
		assertTrue(Resolver.checkRegExp("MI:0000", MI)); //auto-ignores "MI:"
		assertTrue(Resolver.checkRegExp("0000", MI));
	}
	
	@Test
	public final void getURI() {
		//standard prefix and id with "banana" prefix
		assertEquals("bioregistry.io/chebi:36927", Resolver.getURI("chebi", "CHEBI:36927"));
		//old ns prefix (should auto-fix as "chebi") and id without "CHEBI:" banana...
		assertEquals("bioregistry.io/chebi:36927", Resolver.getURI("identifiers.org/chebi/", "36927"));
		//similar, but using bioregistry.io/ base, with ending slash, and with banana...
		assertEquals("bioregistry.io/chebi:36927", Resolver.getURI("bioregistry.io/chebi", "CHEBI:36927"));
	}

	@Test
	public final void getCURIE() {
		assertEquals("uniprot:P12345", Resolver.getCURIE("uniprot", "P12345"));
		assertEquals("go:0045202", Resolver.getCURIE("go", "GO:0045202"));
	}

}
