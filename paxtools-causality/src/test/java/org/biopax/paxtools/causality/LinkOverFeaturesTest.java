package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.util.Kronometre;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Ozgun Babur
 */
public class LinkOverFeaturesTest
{
	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	@Ignore
	public void testLinking() throws FileNotFoundException
	{
		Kronometre k = new Kronometre();
		
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));

		LinkerOverFeatures lof = new LinkerOverFeatures();
		Model m = lof.link(model);

		handler.convertToOWL(m, new FileOutputStream(
			"/home/ozgun/Desktop/pattern-matches/linked-temp.owl"));
		
		k.stop();
		k.print();
	}
}
