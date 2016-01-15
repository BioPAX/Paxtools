package org.biopax.paxtools.examples;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
//import org.biopax.paxtools.io.sif.InteractionRule;
//import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.miner.ExtendedSIFWriter;
import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.biopax.paxtools.pattern.miner.SIFInteraction;
import org.biopax.paxtools.pattern.miner.SIFSearcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
//import java.util.Arrays;
import java.util.Set;

/**
 * This example exports A BioPAX model to SIF.
 *
 * To use specific rules uncomment the rule enumeration below.
 *
 */
public final class SifnxExportExample {

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Please run again providing arguments: "
					+ "input(BioPAX OWL file), output");
			System.exit(-1);
		}

		// import BioPAX from OWL file (auto-detects level)
		BioPAXIOHandler biopaxReader = new SimpleIOHandler();
		Model model = biopaxReader.convertFromOWL(new FileInputStream(args[0]));
		
//		SimpleInteractionConverter sic = // add all rules
//        	new SimpleInteractionConverter(SimpleInteractionConverter
//				.getRules(model.getLevel()).toArray(new InteractionRule[]{}));

		/* or - use some of the rules - 
		SimpleInteractionConverter sic = null;
		if (BioPAXLevel.L2.equals(model.getLevel())) {
			sic = new SimpleInteractionConverter(
				new org.biopax.paxtools.io.sif.level2.ComponentRule(),
				new org.biopax.paxtools.io.sif.level2.ConsecutiveCatalysisRule(), 
				new org.biopax.paxtools.io.sif.level2.ControlRule(),
				new org.biopax.paxtools.io.sif.level2.ControlsTogetherRule(), 
				new org.biopax.paxtools.io.sif.level2.ParticipatesRule());
		} else if (BioPAXLevel.L3.equals(model.getLevel())) {
			sic = new SimpleInteractionConverter(
					new org.biopax.paxtools.io.sif.level3.ComponentRule(),
					new org.biopax.paxtools.io.sif.level3.ConsecutiveCatalysisRule(),
					new org.biopax.paxtools.io.sif.level3.ControlRule(),
					new org.biopax.paxtools.io.sif.level3.ControlsTogetherRule(),
					new org.biopax.paxtools.io.sif.level3.ParticipatesRule());
		} else {
			System.err.println("SIF converter does not yet support BioPAX level: " 
					+ model.getLevel());
			System.exit(0);
		}
		*/
//		OutputStream edgeStream = new FileOutputStream(args[1]);
//		OutputStream nodeStream = new FileOutputStream(args[2]);
//        sic.writeInteractionsInSIFNX(model, edgeStream, nodeStream,
//        		null, Arrays.asList("entity/NAME","entity/XREF"),false);

		ModelUtils.mergeEquivalentInteractions(model);

		SIFSearcher searcher = new SIFSearcher(SIFEnum.values());
//		searcher.setBlacklist(blacklist); //good to have a blacklist of ubiquitous molecules
		Set<SIFInteraction> binaryInts = searcher.searchSIF(model);
		OutputStream out = new FileOutputStream(args[1]);
		ExtendedSIFWriter.write(binaryInts, out);
		try {out.close();} catch(Throwable t) {}
	}

}
