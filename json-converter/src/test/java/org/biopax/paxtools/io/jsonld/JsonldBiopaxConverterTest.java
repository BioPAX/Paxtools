package org.biopax.paxtools.io.jsonld;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URI;

import org.junit.jupiter.api.Assertions;

public class JsonldBiopaxConverterTest {

	@Test
	final void test() throws IOException {
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
	final void test2() throws IOException {
		JsonldConverter converter = new JsonldBiopaxConverter();
		// convert owl test file in resource directory to jsonld format
		InputStream in = getClass().getResourceAsStream("/demo-pathway-xsd.owl");
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
				() -> Assertions.assertTrue(res.contains("@id\" : \"chebi:20")), //unchanged
				() -> Assertions.assertTrue(res.contains("\"@id\" : \"http://www.biopax.org/release/biopax-level3.owl#displayName\""))
		);
	}

	@Test
	final void test3() throws IOException {
		JsonldConverter converter = new JsonldBiopaxConverter();
		// convert owl test file in resource directory to jsonld format
		InputStream in = getClass().getResourceAsStream("/demo-pathway-noxsd.owl");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		converter.convertToJsonld(in, baos);
		String res = baos.toString("UTF-8");
		Assertions.assertAll(
				() -> Assertions.assertThrows(IllegalArgumentException.class, () -> URI.create("http://")),
				() -> Assertions.assertDoesNotThrow(() -> URI.create("bioregistry.io/chebi:20")),
				() -> Assertions.assertDoesNotThrow(() -> URI.create("chebi:20")),
				() -> Assertions.assertDoesNotThrow(() -> URI.create("http://bioregistry.io/chebi:28")),
				() -> Assertions.assertTrue(res.contains("@id\" : \"http://bioregistry.io/chebi:28")),
				() -> Assertions.assertTrue(res.contains("\"@id\" : \"http://www.biopax.org/release/biopax-level3.owl#displayName\"")),
				() -> Assertions.assertTrue(res.contains("@id\" : \"chebi:28"))
		);
	}

}
