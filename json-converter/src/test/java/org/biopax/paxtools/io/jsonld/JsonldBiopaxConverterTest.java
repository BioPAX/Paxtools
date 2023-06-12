package org.biopax.paxtools.io.jsonld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

public class JsonldBiopaxConverterTest {

	@Test
	public final void test() throws IOException {
		File jsonldTestFileName = File.createTempFile("test", ".jsonld");
		File rdfTestFileName = File.createTempFile("test", ".rdf");

		JsonldConverter intf = new JsonldBiopaxConverter();

		// convert owl test file in resource directory to jsonld format
		InputStream in = getClass().getResourceAsStream("/PC2v5test-Signaling-By-BMP-Pathway-REACT_12034.2.owl");
		intf.convertToJsonld(in, new FileOutputStream(jsonldTestFileName));

		// convert jsonld test file back to rdf format
		InputStream inputLD = new FileInputStream(jsonldTestFileName);
		OutputStream outRDF = new FileOutputStream(rdfTestFileName);
		intf.convertFromJsonld(inputLD, outRDF);
	}

}
