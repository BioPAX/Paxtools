package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author Ozgun Babur
 */
public class SBGNConverterTest
{
	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void testSBGNConversion()
	{
		InputStream in = getClass().getResourceAsStream("/merge-bmp.owl");
		Model level3 = handler.convertFromOWL(in);

		L3ToSBGNPDConverter.writeSBGN(level3, "target/merge-bmp.sbgn");
	}
}
