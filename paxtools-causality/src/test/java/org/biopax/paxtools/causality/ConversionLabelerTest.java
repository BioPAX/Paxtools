package org.biopax.paxtools.causality;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.junit.Test;

import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class ConversionLabelerTest
{
	@Test
	public void testLabelingConversions()
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(getClass().getResourceAsStream("STAT2.owl"));

		EntityReference er = (EntityReference) model.getByID("urn:miriam:uniprot:P52630");

		ConversionTypeLabeler ctl = new ConversionTypeLabeler();
		Map<Conversion,Integer> map = ctl.label(er, model);

		for (Conversion key : map.keySet())
		{
			System.out.print("key = " + key);
			System.out.println("\tvalue = " + map.get(key));
		}
	}
}
