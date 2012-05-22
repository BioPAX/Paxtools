package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.analysis.BFS;
import org.biopax.paxtools.causality.analysis.CausativePathSearch;
import org.biopax.paxtools.causality.model.*;
import org.biopax.paxtools.causality.util.Binomial;
import org.biopax.paxtools.causality.util.Summary;
import org.biopax.paxtools.causality.wrapper.ComplexMember;
import org.biopax.paxtools.causality.wrapper.Graph;
import org.biopax.paxtools.causality.wrapper.PhysicalEntityWrapper;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.algorithm.Direction;
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

	public static Map<EntityReference, Set<EntityReference>>[] getAffectedDownstreamMaps(Graph graph)
	{
		Map<EntityReference, Set<EntityReference>>[] map = new Map[2];
		map[0] = new HashMap<EntityReference, Set<EntityReference>>();
		map[1] = new HashMap<EntityReference, Set<EntityReference>>();

		int i = 0;
		for (ProteinReference pr : graph.getModel().getObjects(ProteinReference.class))
		{
			if (!isHuman(pr)) continue;

			System.out.print(".");
			i++;
			if (i % 150 == 0) System.out.println();
			
			map[0].put(pr, new HashSet<EntityReference>());
			map[1].put(pr, new HashSet<EntityReference>());

//			if (pr.getRDFId().equals("urn:miriam:uniprot:P02771"))
//			{
//				System.out.println();
//			}
			
			BFS bfs = new BFS(graph.getForAll(pr.getEntityReferenceOf()), null, Direction.UPSTREAM, 3, true);
			Map<GraphObject, Integer> labels = bfs.run();
			for (GraphObject go : labels.keySet())
			{
				if (labels.get(go) == 0) continue;

				if (go instanceof PhysicalEntityWrapper)
				{
					PhysicalEntityWrapper pew = (PhysicalEntityWrapper) go;
					PhysicalEntity pe = pew.getPhysicalEntity();
					if (pe instanceof SimplePhysicalEntity && pew.getPathSign() != 0)
					{
						EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
						if (er != null && er != pr)
						{
							map[pew.getPathSign() == 1 ? 0: 1].get(pr).add(er);
						}
					}
				}
			}
			graph.clear();
		}
		System.out.println();
		System.out.println("Positives = " + countNonEmpty(map[0]));
		System.out.println("Negatives = " + countNonEmpty(map[1]));
		return map;
	}

	private static int countNonEmpty(Map<EntityReference, Set<EntityReference>> map)
	{
		int cnt = 0;
		for (EntityReference er : map.keySet())
		{
			if (!map.get(er).isEmpty()) cnt++; 
		}
		return cnt;
	}
	
	private static boolean isHuman(ProteinReference pr)
	{
		return pr.getOrganism() != null && pr.getOrganism().getDisplayName().equals("Homo sapiens");
	}

	public static Map<EntityReference, int[]> getActivityPredictions(
		Map<EntityReference, Set<EntityReference>>[] down, AlterationProvider ap, int i)
	{
		Map<EntityReference, int[]> map = new HashMap<EntityReference, int[]>();

		for (EntityReference er : down[0].keySet())
		{
			map.put(er, new int[2]);

			for (EntityReference dEr : down[0].get(er))
			{
				String id = getEGID(dEr);
				if (id != null)
				{
					AlterationPack alt = ap.getAlterations(id);
					
					if (alt != null)
					{
						Change ch = alt.getChange(Alteration.ANY, i);
						if (ch == Change.ACTIVATING) map.get(er)[0]++;
						else if (ch == Change.INHIBITING) map.get(er)[1]++;
					}
				}
			}
			for (EntityReference dEr : down[1].get(er))
			{
				String id = getEGID(dEr);
				if (id != null)
				{
					AlterationPack alt = ap.getAlterations(id);

					if (alt != null)
					{
						Change ch = alt.getChange(Alteration.ANY, i);
						if (ch == Change.ACTIVATING) map.get(er)[1]++;
						else if (ch == Change.INHIBITING) map.get(er)[0]++;
					}
				}
			}
		}
		return map;
	}
	
	private static String getEGID(EntityReference er)
	{
		for (Xref xref : er.getXref())
		{
			if (xref.getDb().equals("Entrez Gene")) return xref.getId();
		}
		return null;
	}
	
	public static void predictActivityFromDownstream(Model model, Set<String> ubiqueIDs, 
		AlterationProvider ap)
	{
		Graph graph = new Graph(model, ubiqueIDs);
		Map<EntityReference, Set<EntityReference>>[] downMaps = getAffectedDownstreamMaps(graph);
		int expSize = ap.getAlterations("367").get(Alteration.ANY).length;
		System.out.println("expSize = " + expSize);

		Map<EntityReference, int[]> count = new HashMap<EntityReference, int[]>();
		
		for (int i = 0; i < expSize; i++)
		{
			Map<EntityReference, int[]> pred = getActivityPredictions(downMaps, ap, i);

			for (EntityReference er : pred.keySet())
			{
				AlterationPack alt = ap.getAlterations(getEGID(er));
				
				if (alt != null)
				{
					if (!count.containsKey(er)) count.put(er, new int[4]);

					Change ch = alt.getChange(Alteration.ANY, i);
					if (ch.isAltered())
					{
						int act = pred.get(er)[0];
						int inh = pred.get(er)[1];
						
						if (Binomial.getPval(act, inh) < 0.5)
						{
								 if (ch == Change.ACTIVATING && act > inh) count.get(er)[0]++;
							else if (ch == Change.ACTIVATING && act < inh) count.get(er)[1]++;
							else if (ch == Change.INHIBITING && act > inh) count.get(er)[2]++;
							else if (ch == Change.INHIBITING && act < inh) count.get(er)[3]++;
						}
					}
				}
			}
		}

		for (EntityReference er : count.keySet())
		{
			int[] c = count.get(er);
			
			if (Summary.sum(c) > 1)
			{
				System.out.println(er.getDisplayName() + "\t" + c[0] + "\t" + c[1] + "\t" + c[2] + "\t" + c[3]);
			}
		}
	}
	
	private static AlterationPack getAPack(AlterationProvider ap)
	{
		AlterationPack pack = null;
		for (int i = 0; i < 1000; i++)
		{
			pack = ap.getAlterations("" + i);
			if (pack != null) break;
		}
		return pack;
	}
}
