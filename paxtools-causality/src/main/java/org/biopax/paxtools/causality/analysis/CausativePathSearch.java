package org.biopax.paxtools.causality.analysis;

import org.biopax.paxtools.causality.model.*;
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
	public List<Path> search(Node source, int distance, double overlapThr)
	{
		AlterationPack pack = source.getAlterations();

		boolean alt_exp = pack.isAltered(Alteration.EXPRESSION);
		boolean alt_prot = pack.isAltered(Alteration.PROTEIN_LEVEL);
		if (!alt_exp && !alt_prot) return Collections.emptyList();

		BFS bfs;
		
		if (alt_prot)
		{
			bfs = new BFS(Collections.singleton(source), null, Direction.UPSTREAM, distance);
		}
		else
		{
			Set<Node> t = getTranscriptionReactions(source);
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
				if (rat > overlapThr)
				{
					pewSet.add(pew);
				}
			}
		}

		final List<Path> result = new ArrayList<Path>();

		if (!pewSet.isEmpty())
		{
			Exhaustive ex = new Exhaustive(source, Direction.UPSTREAM, distance, new PathUser()
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
