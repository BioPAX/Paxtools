package org.biopax.paxtools.io.simpleIO;

import junit.framework.TestCase;
import org.junit.Test;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level3.Xref;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URISyntaxException;

public class SimpleReaderTest extends TestCase
{

	@Test
	public void testSimpleReaderL2() throws URISyntaxException
	{
		//testDirectory(BioPAXLevel.L2); //resources dir
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
			System.out.println("done (model has " + model.getObjects().size() + " objects)");
		}
	}
}
