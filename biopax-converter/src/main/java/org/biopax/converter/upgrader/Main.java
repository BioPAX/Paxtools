/**
 * 
 */
package org.biopax.converter.upgrader;

import java.io.FileInputStream;
import java.io.IOException;

import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.Model;

/**
 * @author rodch
 *
 */
public final class Main {

	/**
	 * @param args biopax file names to convert
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		for (String filename : args) {
			SimpleReader reader = new SimpleReader();
			Model model = reader.convertFromOWL(new FileInputStream(filename));
			Upgrader upgrader = new Upgrader();
			model = upgrader.convert(model);
			if (model != null) {
				SimpleExporter exporter = new SimpleExporter(model.getLevel());
				exporter.convertToOWL(model, System.out);
			}
		}
	}

}
