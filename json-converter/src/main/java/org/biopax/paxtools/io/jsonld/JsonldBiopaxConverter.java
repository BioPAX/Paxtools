package org.biopax.paxtools.io.jsonld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonldBiopaxConverter implements JsonldConverter {

	private final static Logger LOG = LoggerFactory.getLogger(JsonldBiopaxConverter.class);

	/*
	 * Convert inputstream in owl/rdf format to outputsream in jsonld format
	 */
	public void convertToJsonld(InputStream in, OutputStream os)
			throws IOException {
		
		File inputProcessedFile = preProcessFile(in);
		LOG.info("OWl File processed successfully ");
		
		// print current time
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		LOG.info("Conversion RDF to JSONLD started "
				+ sdf.format(Calendar.getInstance().getTime()));

		// create an empty model
		com.hp.hpl.jena.rdf.model.Model modelJena = ModelFactory.createDefaultModel();
		InputStream internalInputStream = new FileInputStream(inputProcessedFile);

		// read the RDF/XML file
		RDFDataMgr.read(modelJena, internalInputStream, Lang.RDFXML);
		LOG.info("Read into Model finished "
				+ sdf.format(Calendar.getInstance().getTime()));

		RDFDataMgr.write(os, modelJena, Lang.JSONLD);
		LOG.info("Conversion RDF to JSONLD finished "
				+ sdf.format(Calendar.getInstance().getTime()));
		LOG.info(" JSONLD file " + " is written successfully.");

		try { //close, flush quietly
			os.close();
		} catch(Exception e) {}

	}

	
	/*
	 * Convert inputstream in jsonld format to outputsream if owl/rdf format
	 */
	public void convertFromJsonld(InputStream in, OutputStream out) {

		com.hp.hpl.jena.rdf.model.Model modelJena = ModelFactory.createDefaultModel();

		if (in == null) {
			throw new IllegalArgumentException("Input File: " + " not found");
		}
		if (out == null) {
			throw new IllegalArgumentException("Output File: " + " not found");
		}

		// read the JSONLD file
		modelJena.read(in, null, "JSONLD");

		RDFDataMgr.write(out, modelJena, Lang.RDFXML);
		LOG.info(" RDF file " + " is written successfully.");

	}

	// Instantiate a simple (StAX based) biopax reader/writer - SimpleIOHandler

	public File preProcessFile(InputStream in) throws IOException {

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		LOG.info("BIOPAX Conversion started "
				+ sdf.format(Calendar.getInstance().getTime()));

		if (in == null) {
			throw new IllegalArgumentException("Input File: " + " is not found");
		}

		SimpleIOHandler simpleIO = new SimpleIOHandler(BioPAXLevel.L3);

		// create a Paxtools Model from the BioPAX L3 RDF/XML input file
		// (stream)

		org.biopax.paxtools.model.Model model = simpleIO.convertFromOWL(in);

		// set for the IO to output full URIs:

		simpleIO.absoluteUris(true);

		File fullUriBiopaxInput = File.createTempFile("biopaxTemp", "owl");

		fullUriBiopaxInput.deleteOnExit(); // delete on JVM exits
		FileOutputStream outputStream = new FileOutputStream(fullUriBiopaxInput);

		// write to an output stream (back to RDF/XML)

		simpleIO.convertToOWL((org.biopax.paxtools.model.Model) model,
				outputStream); // it closes the stream internally

		model = null;

		LOG.info("BIOPAX Conversion finished "
				+ sdf.format(Calendar.getInstance().getTime()));
		return fullUriBiopaxInput;
	}

}
