package org.biopax.paxtools.causality.analysis;

import org.biopax.paxtools.causality.model.*;
import org.biopax.paxtools.causality.wrapper.Graph;
import org.biopax.paxtools.causality.wrapper.PhysicalEntityWrapper;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;

import java.util.*;

/**
 * This class searches for causative paths that can explain the alteration of the source node.
 *
 * @author Ozgun Babur
 */
public class CausativePathSearch
{
	public Map<Node, Map<Integer, Integer>[]> labelGraph(Graph graph, int distance, 
		double overlapThr)
	{
		Map<Node, Map<Integer, Integer>[]> label = new HashMap<Node, Map<Integer, Integer>[]>();

		Map<Node, Set<String>[]> seen = new HashMap<Node, Set<String>[]>();

		for (Node target : graph.getBreadthNodes())
		{
			AlterationPack altTarget = target.getAlterations();

			if (altTarget == null || !altTarget.isAltered()) continue;

			List<Path> paths = search(target, distance, overlapThr);

			for (Path path : paths)
			{
				assert path.isReverse();
				assert path.getFirstNode() == target;
				
				Node source = path.getLastNode();
				AlterationPack altSource = source.getAlterations();
				assert altSource.isAltered();

				recordPaths(label, seen, altTarget, altSource, path, true);
				recordPaths(label, seen, altTarget, altSource, path, false);
			}
		}
		return label;
	}

	/**
	 * Fills in the label map, using the causative path, with case numbers and their count for
	 * predicting up or down.
	 *
	 * @param label total counts
	 * @param seen encountered alteration pairs
	 * @param altTarget target alterations
	 * @param altSource source alterations
	 * @param path the causative path
	 * @param up indicates the case where target is up (true) or down (false).
	 */
	protected void recordPaths(Map<Node, Map<Integer, Integer>[]> label,
		Map<Node, Set<String>[]> seen, AlterationPack altTarget, AlterationPack altSource,
		Path path, boolean up)
	{
		List<Integer> inds = altTarget.getParallelChangedIndexes(
			altSource, path.getSign() == 1, up);

		if (!inds.isEmpty())
		{
			String altKey = altSource.getId() + "-" + altTarget.getId();

			Map<Node, Integer> sm = path.getIntermediateSignMapping(up? 1 : -1);
			for (Node node : sm.keySet())
			{
				Integer sign = sm.get(node);

				if (!seen.containsKey(node))
				{
					seen.put(node, new Set[]{new HashSet<String>(), new HashSet<String>()});
				}

				if (!seen.get(node)[sign == 1 ? 0 : 1].contains(altKey))
				{
					seen.get(node)[sign == 1 ? 0 : 1].add(altKey);

					if (!label.containsKey(node))
					{
						label.put(node, new Map[]{new HashMap<Integer, Integer>(),
							new HashMap<Integer, Integer>()});
					}

					Map<Integer, Integer> map = label.get(node)[sign == 1 ? 0 : 1];
					for (Integer ind : inds)
					{
						if (!map.containsKey(ind)) map.put(ind, 1);
						else map.put(ind, map.get(ind) + 1);
					}
				}
			}
		}
	}

	public List<Path> search(Node target, int distance, double overlapThr)
	{
		AlterationPack pack = target.getAlterations();

		boolean alt_exp = pack.isAltered(Alteration.EXPRESSION);
		boolean alt_prot = pack.isAltered(Alteration.PROTEIN_LEVEL);
		if (!alt_exp && !alt_prot) return Collections.emptyList();

		BFS bfs;
		
		if (alt_prot)
		{
			bfs = new BFS(Collections.singleton(target), null, Direction.UPSTREAM, distance);
		}
		else
		{
			Set<Node> t = getTranscriptionReactions(target);
			if (t.isEmpty()) return Collections.emptyList();

			bfs = new BFS(t, null, Direction.UPSTREAM, distance);
		}

		Map<GraphObject,Integer> distMap = bfs.run();

		final Set<PhysicalEntityWrapper> pewSet = new HashSet<PhysicalEntityWrapper>();
		
		for (GraphObject go : distMap.keySet())
		{
			if (go instanceof PhysicalEntityWrapper)
			{
				PhysicalEntityWrapper pew = (PhysicalEntityWrapper) go;
				if (pew.getPathSign() == 0) continue;

				AlterationPack pack2 = pew.getAlterations();
				if (pack2 == null) continue;
				if (pack == pack2) continue;

				double rat = pack.getParallelChangeRatio(pack2, pew.getPathSign() == 1);
				if (rat >= overlapThr)
				{
					pewSet.add(pew);
				}
			}
		}

		final List<Path> result = new ArrayList<Path>();

		if (!pewSet.isEmpty())
		{
			Exhaustive ex = new Exhaustive(target, Direction.UPSTREAM, distance, new PathUser()
			{
				@Override
				public void processPath(Path path)
				{
					if (pewSet.contains(path.getLastNode()))
					{
						try
						{
							result.add((Path) path.clone());
						}
						catch (CloneNotSupportedException e)
						{
							e.printStackTrace();
							throw new RuntimeException("Clone should have been supported here.");
						}
					}
				}
			}, distMap.keySet());

			ex.run();
		}

		for (GraphObject go : distMap.keySet())
		{
			go.clear();
		}
		return result;
	}

	protected Set<Node> getTranscriptionReactions(Node node)
	{
		Set<Node> t = new HashSet<Node>();
		for (Edge edge : node.getUpstream())
		{
			if (edge.isTranscription()) t.add((Node) edge.getSourceNode());
		}
		return t;
	}
}
