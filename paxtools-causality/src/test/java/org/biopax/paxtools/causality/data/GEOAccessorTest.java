package org.biopax.paxtools.causality.data;

import org.biopax.paxtools.causality.model.Alteration;
import org.biopax.paxtools.causality.model.Change;
import org.junit.Test;

import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class GEOAccessorTest
{
	@Test
	public void readTheSeries()
	{
		String gseID = "GSE3325";
		String id = "367";
		int[] control = new int[]{0,1,2};
		int[] test = new int[]{3,4,5};
		
		GEOAccessor acc = new GEOAccessor(gseID, test, control);
		Map<Alteration,Change[]> alt = acc.getAlterations(id);
		for (Change ch : alt.get(Alteration.EXPRESSION))
		{
			System.out.println(ch);
		}
	}
}
