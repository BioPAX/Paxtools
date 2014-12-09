
/**
 * @author yurishiyanov, rodche (polished, re-factored, moved here)
 * 
 * Re-factored and added to Paxtools from the 
 * original https://github.com/BaderLab/biopax-jsonld project.
 * 
 * A BioPAX to/from JSON-LD format converter.
 * 
 * Requirements:
 * 
 * Needs jdk 1.7 and Jena library verion  2.12.0 or later.
 * All dependencies outlined in maven pom.xml file.
 * 
 * Maximum tested size of owl file is 400M, it needs PC with minimum 12 G memory 
 * as processing of the file involved creation Jena model in the memory. 
 * This step is very memory intensive.
 * 
 * An example how to run conversion methods from java code is in Main.java file.
 * 
 * In short:
 * To convert owl/rdf file to jsonld format use method convertToJsonld in interface JsonldConverter:
 * 
 * convertToJsonld(InputStream in, OutputStream os).
 * 
 * To convert jsonld file from jsonld to RDF format use method convertFromJsonld in interface JsonldConverter:
 * 
 * convertFromJsonld(InputStream in, OutputStream out)
 * 
 */
package org.biopax.paxtools.io.jsonld;

