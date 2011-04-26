package org.biopax.paxtools.examples;

import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.UnificationXref;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class SimpleIOExample {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Please run again providing one argument, "
					+ "a BioPAX OWL file path/name.");
			System.exit(-1);
		}

		// import BioPAX from OWL file (auto-detects level)
		BioPAXIOHandler biopaxIO = new SimpleIOHandler();
		Model model = biopaxIO.convertFromOWL(new FileInputStream(args[0]));
		// write (as BioPAX OWL)
		output(model);
		// TODO play with model...
		/*
		 * if(model == null || model.getLevel() != BioPAXLevel.L3) { throw new
		 * IllegalArgumentException(" is not supported!"); }
		 */

		// Well, let's do something with a (new) BioPAX model
		
		BioPAXFactory bioPAXFactory = new Level3FactoryImpl(); 
		// - one can also use "other implementations" or the level's default:
		// BioPAXFactory bioPAXFactory = BioPAXLevel.L3.getDefaultFactory()
		
		Model model2 = bioPAXFactory.createModel();
		
		// set default name space prefix (base);
		model2.getNameSpacePrefixMap().put("", "http://baderlab.org#");
		// create and add a new element to the model;
		// still, rdfid must be set in full (not just "#xref_P62158")
		UnificationXref uxref = model2.addNew(UnificationXref.class,
				"http://baderlab.org#xref_P62158");
		uxref.setDb("uniprotkb");
		uxref.setId("P62158");
		// using absolute (ext.) URI as id
		ProteinReference prf = model2.addNew(ProteinReference.class,
				"urn:miriam:uniprot:P62158");
		prf.setDisplayName("CALM_HUMAN");
		prf.addXref(uxref);
		// (do not need to explicitly add objects to the model)
		// write
		output(model2);
		// compare this output with the previous one (see IDs and references)	 
	}

	
	public  static void output(Model model) throws IOException {
		BioPAXIOHandler simpleExporter = new SimpleIOHandler();
		OutputStream out = new ByteArrayOutputStream();
		simpleExporter.convertToOWL(model, out);
		System.out.println(out + "\n");
	}

}
