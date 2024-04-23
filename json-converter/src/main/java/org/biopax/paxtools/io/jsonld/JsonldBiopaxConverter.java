package org.biopax.paxtools.io.jsonld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonldBiopaxConverter implements JsonldConverter {

	private final static Logger LOG = LoggerFactory.getLogger(JsonldBiopaxConverter.class);

	/*
	 * Convert biopax owl (rdf/xml) to jsonld format.
	 */
	public void convertToJsonld(InputStream in, OutputStream os) throws IOException {
		File tmpFile = preProcessFile(in); //in gets there closed
		Model modelJena = ModelFactory.createDefaultModel();
		in = new FileInputStream(tmpFile);
		RDFDataMgr.read(modelJena, in, Lang.RDFXML);
		RDFDataMgr.write(os, modelJena, Lang.JSONLD);
		LOG.info("BioPAX RDFXML to JSONLD finished");
		try { //close, flush quietly
			in.close();
			os.close();
			tmpFile.delete();
		} catch(Exception e) {}
	}

	
	/*
	 * Convert jsonld back to rdf/xml
	 * if that jsonld was converted from rdf/xml (e.g. biopax) originally
	 */
	public void convertFromJsonld(InputStream in, OutputStream out) {
		if (in == null) {
			throw new IllegalArgumentException("Input File: " + " not found");
		}
		if (out == null) {
			throw new IllegalArgumentException("Output File: " + " not found");
		}
		Model modelJena = ModelFactory.createDefaultModel();
		modelJena.read(in, null, "JSONLD");
		RDFDataMgr.write(out, modelJena, Lang.RDFXML);
		LOG.info("JSONLD to RDFXML finished");
	}

	/**
	 * Converts the BioPAX data (stream) to an equivalent temporary
	 * BioPAX RDF/XML file that contains absolute instead of (possibly)
	 * relative URIs for all the BioPAX elements out there; and returns that file.
	 * This is required due to a bug in Jena lib that results in inserting '#' inside the URIs...
	 *
	 * @param in biopax input stream
	 * @return a temporary file
	 * @throws IOException
     */
	public File preProcessFile(InputStream in) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("Input File: " + " is not found");
		}
		SimpleIOHandler simpleIO = new SimpleIOHandler(BioPAXLevel.L3);
		// create a Paxtools Model from the BioPAX RDF/XML input stream
		org.biopax.paxtools.model.Model model = simpleIO.convertFromOWL(in);//also closes the input stream
//		model.setXmlBase("");
		simpleIO.absoluteUris(true); //forces absolute URIs in the output!
		File tmpf = File.createTempFile("paxtools", ".owl");
		tmpf.deleteOnExit(); // delete on JVM exits
		FileOutputStream outputStream = new FileOutputStream(tmpf);
		// write to an output stream (back to RDF/XML)
		simpleIO.convertToOWL(model,	outputStream); //also closes the output stream
		return tmpf;
	}

}
