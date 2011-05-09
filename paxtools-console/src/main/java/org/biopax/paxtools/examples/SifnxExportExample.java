package org.biopax.paxtools.examples;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public final class SifnxExportExample {

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Please run again providing arguments: "
					+ "input(BioPAX OWL file), edgeOutput, nodeOutput");
			System.exit(-1);
		}

		// import BioPAX from OWL file (auto-detects level)
		BioPAXIOHandler biopaxReader = new SimpleIOHandler();
		Model model = biopaxReader.convertFromOWL(new FileInputStream(args[0]));
		
		SimpleInteractionConverter sic = // add all rules
        	new SimpleInteractionConverter(SimpleInteractionConverter
				.getRules(model.getLevel()).toArray(new InteractionRule[]{}));

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
		
		EditorMap editorMap = SimpleEditorMap.get(model.getLevel());
		OutputStream edgeStream = new FileOutputStream(args[1]);
		OutputStream nodeStream = new FileOutputStream(args[2]);
        sic.writeInteractionsInSIFNX(model, edgeStream, nodeStream, 
        		null, Arrays.asList("entity/NAME","entity/XREF"));
	}

}
