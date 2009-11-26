package org.biopax.paxtools.io.jena;

import org.junit.Test;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.physicalEntity;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * User: demir Date: Nov 16, 2007 Time: 1:22:07 AM
 */
public class EquivalenceTest
{
	@Test
	public void loadAndCheckForEquivalentPEPs() throws URISyntaxException
	{
		JenaIOHandler jenaIOHandler = new JenaIOHandler(null, BioPAXLevel.L2);
//		Model merged = jenaIOHandler.factory.createModel();
//		Merger merger = new Merger(jenaIOHandler.editorMap);
		String pathname = "/samples/L2";
		File testDir = new File(getClass().getResource(pathname).toURI());
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return (name.endsWith(".owl"));
			}
		};

		for (String s : testDir.list(filter))
		{
			try
			{
				readAndCheckEquivalents(pathname, s, jenaIOHandler);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}


	}

	private void readAndCheckEquivalents(String pathname, String s,
	                                     JenaIOHandler jenaIOHandler)
		throws IOException
	{
		InputStream in = getClass().getResourceAsStream(pathname + "/" + s);
		Model level2 =
			jenaIOHandler.convertFromOWL(in);

		Set<physicalEntity> bpeSet =
			level2.getObjects(physicalEntity.class);
		for (physicalEntity pe : bpeSet)
		{
			Set<physicalEntityParticipant> pepSet = pe.isPHYSICAL_ENTITYof();
			for (physicalEntityParticipant pep : pepSet)
			{
				for (physicalEntityParticipant pep2 : pepSet)
				{
					pep.isInEquivalentState(pep2);
				}
			}

		}
	}
}
