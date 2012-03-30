package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.analysis.CausativePathSearch;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.AlterationProvider;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.causality.model.Path;
import org.biopax.paxtools.causality.wrapper.Graph;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.query.model.GraphObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides static methods for causality searches.
 *
 * @author Ozgun Babur
 */
public class CausalityExecuter
{
	public static List<Path> findCausativePaths(Model model, AlterationProvider ap, int limit,
		double alterationThr, Set<String> ubiqueIDs)
	{
		List<Path> result = new ArrayList<Path>();

		Graph graph = new Graph(model, ubiqueIDs);
		graph.setAlterationProvider(ap);
		CausativePathSearch cps = new CausativePathSearch();

		for (EntityReference er : model.getObjects(EntityReference.class))
		{
			for (SimplePhysicalEntity pe : er.getEntityReferenceOf())
			{
				for (Node node : graph.getAllWrappers(pe))
				{
					AlterationPack pack = ap.getAlterations(node);

					if (pack != null && pack.getAlteredRatio() > alterationThr)
					{
						result.addAll(cps.search(node, limit, alterationThr));
					}
				}
			}
		}
		return result;
	}
}
