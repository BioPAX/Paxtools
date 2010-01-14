package org.biopax.paxtools.converter;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

public class OneTwoThreeTest {

	@Test
	public final void testFilter() throws IOException {
		SimpleReader reader = new SimpleReader();
		Model model = reader.convertFromOWL(getClass().getResourceAsStream("/biopax-example-short-pathway.owl"));
		model = (new OneTwoThree()).filter(model);
		if (model != null) {
			SimpleExporter exporter = new SimpleExporter(model.getLevel());
			exporter.convertToOWL(model, new FileOutputStream("target/converted.owl"));
		}
	}

	@Test
	public final void testFilterBigger() throws IOException {
		SimpleReader reader = new SimpleReader();
		Model model = reader.convertFromOWL(getClass().getResourceAsStream("/biopax-example-ecocyc-glycolysis.owl"));
		model = (new OneTwoThree()).filter(model);
		if (model != null) {
			SimpleExporter exporter = new SimpleExporter(model.getLevel());
			exporter.convertToOWL(model, new FileOutputStream("target/converted-big.owl"));
		}
	}
	
}
