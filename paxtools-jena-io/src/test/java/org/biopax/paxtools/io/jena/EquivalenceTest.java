package org.biopax.paxtools.io.jena;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * User: demir Date: Nov 16, 2007 Time: 1:22:07 AM
 */
public class EquivalenceTest
{
	
	@Test
	public void loadAndCheckForEquivalentPEPs() throws URISyntaxException, IOException
	{
		JenaIOHandler jenaIOHandler = new JenaIOHandler(null, BioPAXLevel.L2);
		String pathname =  File.separator + "biopax_id_557861_mTor_signaling.owl";
		readAndCheckEquivalents(pathname, pathname, jenaIOHandler);
	}

	private void readAndCheckEquivalents(String pathname, String s,
	                                     JenaIOHandler jenaIOHandler)
		throws IOException
	{
		InputStream in = getClass().getResourceAsStream(s);
		Model level2 = jenaIOHandler.convertFromOWL(in);

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
