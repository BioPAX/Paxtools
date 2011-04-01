package org.biopax.paxtools.converter;

import org.biopax.paxtools.io.*;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.*;

public class OneTwoThreeTest {

    @Test
	public final void testFilter() throws IOException {
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(
			getClass().getClassLoader()
				.getResourceAsStream("biopax-example-short-pathway.owl"));
		model = (new OneTwoThree()).filter(model);
		if (model != null) {
			io.convertToOWL(model, new FileOutputStream(
					getClass().getClassLoader().getResource("").getFile() 
	        		+ File.separator + "converted.owl"));
		}
	}

    @Test
	public final void testFilterBigger() throws IOException {
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(
			getClass().getClassLoader()
				.getResourceAsStream("biopax-example-ecocyc-glycolysis.owl"));
		model = (new OneTwoThree()).filter(model);
		if (model != null) {
			io.convertToOWL(model, new FileOutputStream(
					getClass().getClassLoader().getResource("").getFile() 
	        		+ File.separator + "converted-big.owl"));
		}
	}
	
	//@Test
	public final void testFilterOthers() throws Throwable
	{
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(new FileInputStream(
			"/D:/Ozgun/chibe1x/samples/biopax-files/NGF-independant TRKA activation.owl"));
		model = (new OneTwoThree()).filter(model);
		if (model != null) {
			io.convertToOWL(model, new FileOutputStream("/D:/Ozgun/temp/temp.owl"));
		}
	}
}
