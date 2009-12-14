package org.biopax.converter.level;

import java.io.FileInputStream;
import java.io.IOException;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.Model;

public final class LevelDefault {
	/**
	 * Converts Level1 to Level2 automatically.
	 * When input data is L2 or L3, it passes 
	 * through the PaxTools reader/exporter.
	 * 
	 * TODO validate/normalize L2 and L3 data
	 * 
	 * @param args biopax file names to convert
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		for (String filename : args) {
			SimpleReader reader = new SimpleReader();
			Model model = reader.convertFromOWL(new FileInputStream(filename));
			if (model != null) {
				SimpleExporter exporter = new SimpleExporter(model.getLevel());
				exporter.convertToOWL(model, System.out);
			}
		}
	}
}
