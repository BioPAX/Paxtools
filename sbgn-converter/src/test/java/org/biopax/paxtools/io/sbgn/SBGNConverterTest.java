package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Test;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ozgun Babur
 */
public class SBGNConverterTest
{
	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void testSBGNConversion() throws JAXBException, IOException, SAXException
	{
		String input = "/AR-TP53";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);

		System.out.println("level3.getObjects().size() = " + level3.getObjects().size());

		String out = "target" + input + ".sbgn";
		L3ToSBGNPDConverter.writeSBGN(level3, out);

		File outFile = new File(out);
		boolean isValid = SbgnUtil.isValid(outFile);

		if (isValid)
			System.out.println ("Validation succeeded");
		else
			System.out.println ("Validation failed");

		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn)unmarshaller.unmarshal (outFile);

		System.out.println("result = " + result);
	}
}
