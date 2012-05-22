package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.util.Histogram;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Provenance;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Ozgun Babur
 */
public class ModificationMatcherTest
{
	@Test
	@Ignore
	public void testModificationOverlap() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/cpath2_with_HPRD.owl"));

		ModificationMatcher mm = new ModificationMatcher();
		Histogram hist = mm.getModificationFeatureOverlapHistogram(model);
		hist.print();
		for (Provenance prov : mm.tc.keySet())
		{
			System.out.println("prov = " + prov);
			mm.tc.get(prov).print(1);
		}
	}
}
