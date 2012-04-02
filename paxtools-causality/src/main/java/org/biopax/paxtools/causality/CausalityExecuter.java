package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.analysis.CausativePathSearch;
import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.AlterationProvider;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.causality.model.Path;
import org.biopax.paxtools.causality.wrapper.ComplexMember;
import org.biopax.paxtools.causality.wrapper.Graph;
import org.biopax.paxtools.causality.wrapper.PhysicalEntityWrapper;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.query.model.GraphObject;

import java.util.*;

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

					if (pack != null && pack.getAlteredRatio() >= alterationThr)
					{
						result.addAll(cps.search(node, limit, alterationThr));
					}
				}
			}
		}
		return result;
	}
	
	public static Map<PhysicalEntity, Map<Integer, Integer>[]> labelGraph(Model model, 
		AlterationProvider ap, int limit, double alterationThr, Set<String> ubiqueIDs)
	{
		Map<PhysicalEntity, Map<Integer, Integer>[]> label = 
			new HashMap<PhysicalEntity, Map<Integer, Integer>[]>();
		
		Graph graph = new Graph(model, ubiqueIDs);
		graph.setAlterationProvider(ap);
		CausativePathSearch cps = new CausativePathSearch();

		Map<Node, Map<Integer, Integer>[]> nodeMap = cps.labelGraph(graph, limit, alterationThr);

		for (Node node : nodeMap.keySet())
		{
			if (node instanceof PhysicalEntityWrapper && !(node instanceof ComplexMember))
			{
				PhysicalEntity pe = ((PhysicalEntityWrapper) node).getPhysicalEntity();
				label.put(pe, nodeMap.get(node));
			}
		}

		return label;
	}
}
