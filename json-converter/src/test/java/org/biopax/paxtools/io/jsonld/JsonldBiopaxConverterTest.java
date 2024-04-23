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
		InputStream in = getClass().getResourceAsStream("/pc14-test.owl");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		converter.convertToJsonld(in, baos);
		String res = baos.toString("UTF-8");
		Assertions.assertAll(
				() -> Assertions.assertThrows(IllegalArgumentException.class, () -> URI.create("http://")), //bad URI
				() -> Assertions.assertDoesNotThrow(() -> URI.create("bioregistry.io/chebi:18367")), //valid URI but not good for LD (LinkedData)
				() -> Assertions.assertDoesNotThrow(() -> URI.create("chebi:18367")), // valid URI (CURIE)
				() -> Assertions.assertDoesNotThrow(() -> URI.create("http://bioregistry.io/chebi:18367")), //good valid absolute URI
				() -> Assertions.assertTrue(res.contains("@id\": \"http://bioregistry.io/chebi:18367")),
				() -> Assertions.assertTrue(res.contains("@id\": \"http://bioregistry.io/mi:0361")),//as long as it has 'http://' (valid abs. uri w/o schema would fail here due Jena bug)
				() -> Assertions.assertTrue(res.contains("@id\": \"chebi:18367")) //unchanged
		);
	}

}
