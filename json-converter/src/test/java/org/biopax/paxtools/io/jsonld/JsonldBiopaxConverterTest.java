package org.biopax.paxtools.io.jsonld;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URI;

import org.junit.jupiter.api.Assertions;

public class JsonldBiopaxConverterTest {

	@Test
	final void testPc5BmpSignalingPathway() throws IOException {
		File jsonldTestFileName = File.createTempFile("test", ".jsonld");
		File rdfTestFileName = File.createTempFile("test", ".rdf");

		JsonldConverter converter = new JsonldBiopaxConverter();
		// convert owl test file in resource directory to jsonld format
		InputStream in = getClass().getResourceAsStream("/PC2v5test-Signaling-By-BMP-Pathway-REACT_12034.2.owl");
		converter.convertToJsonld(in, new FileOutputStream(jsonldTestFileName));

		// convert jsonld test file back to rdf format
		InputStream inputLD = new FileInputStream(jsonldTestFileName);
		OutputStream outRDF = new FileOutputStream(rdfTestFileName);
		converter.convertFromJsonld(inputLD, outRDF);
	}

	@Test
	final void testSomePc14DemoPathway() throws IOException {
		JsonldConverter converter = new JsonldBiopaxConverter();

		// convert owl test file in resource directory to jsonld format
		InputStream in = getClass().getResourceAsStream("/demo-pathway.owl");
		//- there is no rdf:datatype=... anymore; should be fine as the datatypes are defined in the biopax-level3.owl spec!
		//todo: for some reason, Jena libs v4 or v5 fail at e.g. rdf:about="TEST_CHEBI:cs_26d67131a0608673ae6a683d1dad18f7",
		//but jena v3 just prints warnings, e.g.: org.apache.jena.riot - [line: 155, col: 82] {W107} Bad URI: <TEST_CHEBI:cs_26d67131a0608673ae6a683d1dad18f7> Code: 0/ILLEGAL_CHARACTER in SCHEME: The character violates the grammar rules for URIs/IRIs.
		//howver, removing the underscore from TEST_CHEBI - makes those warning/errors go away...

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		converter.convertToJsonld(in, baos);
		String res = baos.toString("UTF-8");
		Assertions.assertAll(
				() -> Assertions.assertThrows(IllegalArgumentException.class, () -> URI.create("http://")), //bad URI
				() -> Assertions.assertDoesNotThrow(() -> URI.create("bioregistry.io/chebi:20")), //valid URI but not good for LD (LinkedData)
				() -> Assertions.assertDoesNotThrow(() -> URI.create("chebi:20")), // valid URI (CURIE)
				() -> Assertions.assertDoesNotThrow(() -> URI.create("http://bioregistry.io/chebi:20")), //good valid absolute URI
				() -> Assertions.assertTrue(res.contains("@id\" : \"http://bioregistry.io/chebi:20")),
				() -> Assertions.assertTrue(res.contains("@id\" : \"http://bioregistry.io/mi:0361")),//as long as it has 'http://' (valid abs. uri w/o schema would fail here due Jena bug)
				() -> Assertions.assertTrue(res.contains("@id\" : \"chebi:20")), //CURIE of a standard/normalized SMR's UnificationXref
				() -> Assertions.assertTrue(res.contains("\"@id\" : \"http://www.biopax.org/release/biopax-level3.owl#displayName\"")),
				() -> Assertions.assertTrue(res.contains("\"displayName\" : \"(+)-camphene\"")), //chebi:20
				() -> Assertions.assertTrue(res.contains("\"id\" : \"CHEBI:20\"")) //with jena v3 (e.g. 3.2.0 or 3.17.0)
		);
	}

}
