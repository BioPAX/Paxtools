package org.biopax.paxtools.io.jsonld;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface JsonldConverter {
	
	/**
	 * Convert inputstream in owl/rdf format to outputsream in jsonld format.
	 * 
	 * @param in input stream (BioPAX RDF/XML data)
	 * @param os output stream (to write the JSON-LD result)
	 * @throws IOException when an I/O error occurs
	 */	
	public void convertToJsonld(InputStream in, OutputStream os) throws IOException;
	
	/**
	 * Convert inputstream in jsonld format to outputsream in owl/rdf format.
	 * 
	 * @param in input stream (JSON-LD data)
	 * @param out output stream (to write the BioPAX RDF/XML result)
	 */	
	 public void convertFromJsonld(InputStream in, OutputStream out);
	
}
