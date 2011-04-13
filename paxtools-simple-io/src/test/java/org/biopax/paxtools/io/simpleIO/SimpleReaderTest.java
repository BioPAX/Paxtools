package org.biopax.paxtools.io.simpleIO;

import junit.framework.TestCase;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.net.URISyntaxException;

/**
 * @deprecated
 */
public class SimpleReaderTest extends TestCase
{

	@Test
	public void testSimpleReaderL2() throws URISyntaxException
	{
		testDirectory(BioPAXLevel.L2);
    }

	@Test
	public void testSimpleReaderL3() throws URISyntaxException
	{
		testDirectory(BioPAXLevel.L3);
	}

	private void testDirectory(BioPAXLevel level) throws URISyntaxException
	{
		TestModelIterator iterator = new TestModelIterator(level, ".");
		while (iterator.hasNext())
		{
			Model model = iterator.next();

		}
	}
}
