package org.biopax.paxtools.examples;

import java.io.*;

import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;

public final class SimpleIOExample {

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Please run again providing one argument, "
					+ "a BioPAX OWL file path/name.");
			System.exit(-1);
		}

		// import BioPAX from OWL file (auto-detects level)
		BioPAXIOHandler biopaxReader = new SimpleReader();
		Model model = biopaxReader.convertFromOWL(new FileInputStream(args[0]));
		// write (as BioPAX OWL)
		output(model);
		// TODO play with model...
		/*
		 * if(model == null || model.getLevel() != BioPAXLevel.L3) { throw new
		 * IllegalArgumentException(" is not supported!"); }
		 */

		// Well, let's do something with a (new) BioPAX model
		
		// BAD practice (causes extra dependencies and/or potential bugs)
/*
		Level3Factory factory = new Level3FactoryImpl();
		Model model1 = new ModelImpl(factory); //this constructor may be made (or is) "protected"
		// create a new empty element
		UnificationXref ref = factory.createUnificationXref(); // not very flexible way...
		ref.setRDFId("http://baderlab.org#xref_P62158"); // rdfid must be set
		ref.setDb("uniprotkb");
		ref.setId("P62158");
		// create another one -
		ProteinReference pr = factory.createProteinReference();
		pr.setRDFId("urn:miriam:uniprot:P62158"); // rdfid must be set
		pr.setDisplayName("CALM_HUMAN");
		pr.addXref(ref);
		// explicitly adding new objects to the model is required!
		model1.add(pr);
		model1.add(ref);
		// write
		output(model1);
*/
		
		// GOOD practice of doing the same as above
		
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
		SimpleExporter simpleExporter = new SimpleExporter(model.getLevel());
		OutputStream out = new ByteArrayOutputStream();
		simpleExporter.convertToOWL(model, out);
		System.out.println(out + "\n");
	}

}
