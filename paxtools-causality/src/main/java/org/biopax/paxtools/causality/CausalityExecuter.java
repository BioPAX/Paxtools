package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.model.AlterationProvider;
import org.biopax.paxtools.causality.model.Path;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides static methods for causality searches.
 *
 * @author Ozgun Babur
 */
public class CausalityExecuter
{
	public static List<Path> findCausativePaths(Model model, AlterationProvider ap, int limit)
	{
		List<Path> result = new ArrayList<Path>();
		for (EntityReference er : model.getObjects(EntityReference.class))
		{
			// if er is altered
			{
				// for each active state of er
				{

				}
			}
		}
		return null;
	}
}
