package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.analysis.BFS;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.wrapperL3.ConversionWrapper;
import org.biopax.paxtools.query.wrapperL3.EdgeL3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class starts a reverse bfs from activate states of an entity, and labels other states of the
 * same entity with with their shortest distance to an active state. And labels the conversions with
 * activity depending their input and output PEs, and their labels. An activation reaction reduces
 * those labels, while inactivation reaction increases it.
 *
 * @author Ozgun Babur
 */
public class ConversionTypeLabeler
{
	public Map<Conversion, Integer> label(EntityReference er, Model model)
	{
		Set<PhysicalEntity> active = getActiveStates(er);
		
		Graph graph = new Graph(model);

		Set<Node> source = new HashSet<org.biopax.paxtools.causality.model.Node>();

		for (PhysicalEntity pe : active)
		{
			source.add((org.biopax.paxtools.causality.model.Node) graph.getGraphObject(pe));
		}

		BFS bfs = new BFS(source, null, Direction.UPSTREAM, 10);

		Map<GraphObject, Integer> map = bfs.run();

		Map<Conversion, Integer> convMap = new HashMap<Conversion, Integer>();

		for (GraphObject go : map.keySet())
		{
			if (go instanceof ConversionWrapper)
			{
				ConversionWrapper w = (ConversionWrapper) go;
				Conversion conv = w.getConversion();
				convMap.put(conv, decideClassOfConversion(conv, map, graph));
			}
		}
		return convMap;
	}
	
	private Integer decideClassOfConversion(Conversion cnv, Map<GraphObject, Integer> map,
		Graph graph)
	{
		Integer left = getMinLabel(cnv.getLeft(), map, graph);
		Integer right = getMinLabel(cnv.getRight(), map, graph);

		System.out.println("left = " + left);
		System.out.println("right = " + right);

		if (left.equals(right)) return 0;
		
		if (cnv.getConversionDirection() == ConversionDirectionType.LEFT_TO_RIGHT ||
			cnv.getConversionDirection() == null)
		{
			return left > right ? 1 : -1;
		}
		else if (cnv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			return right > left ? 1 : -1;
		}
		else
		{
			return 0;
		}
	}

	private Integer getMinLabel(Set<PhysicalEntity> set, Map<GraphObject, Integer> map, Graph graph)
	{
		Integer min = Integer.MAX_VALUE;

		for (PhysicalEntity pe : set)
		{
			Integer lab = map.get(graph.getGraphObject(pe));
			if (lab != null && lab < min) min = lab; 
		}
		return min;
	}

	private Set<PhysicalEntity> getActiveStates(EntityReference er)
	{
		Set<PhysicalEntity> active = new HashSet<PhysicalEntity>();

		for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
		{
			getActiveStatesRecursive(spe, active);
		}
		return active;
	}

	private void getActiveStatesRecursive(PhysicalEntity pe, Set<PhysicalEntity> active)
	{
		if (!pe.getControllerOf().isEmpty()) active.add(pe);

		for (Complex cmp : pe.getComponentOf())
		{
			getActiveStatesRecursive(cmp, active);
		}
	}

	
	//---- Modified wrappers for the analysis -----------------------------------------------------|
	
	class Graph extends org.biopax.paxtools.causality.wrapper.Graph
	{
		public Graph(Model model)
		{
			super(model, null);
		}

		@Override
		public Node wrap(Object obj)
		{
			if (obj instanceof PhysicalEntity)
			{
				return new PhysicalEntityWrapper((PhysicalEntity) obj, this);
			}
			else if (obj instanceof Control)
			{
				return new ControlWrapper((Control) obj, this);
			}
			else return super.wrap(obj);
		}
	}
	
	class PhysicalEntityWrapper extends org.biopax.paxtools.causality.wrapper.PhysicalEntityWrapper
	{
		public PhysicalEntityWrapper(PhysicalEntity pe, 
			org.biopax.paxtools.causality.wrapper.Graph graph)
		{
			super(pe, graph);
			if (pe instanceof SmallMolecule)
			{
				this.setUbique(true);
			}
		}

		@Override
		public void initDownstream()
		{
			for (Interaction inter : getDownstreamInteractions(pe.getParticipantOf()))
			{
				if (inter instanceof Control) continue;
				
				AbstractNode node = (AbstractNode) graph.getGraphObject(inter);

				if (inter instanceof Conversion)
				{
					Conversion conv = (Conversion) inter;
					ConversionWrapper conW = (ConversionWrapper) node;
					if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
						conv.getRight().contains(pe))
					{
						node = conW.getReverse();
					}
				}

				Edge edge = new EdgeL3(this, node, graph);
				this.getDownstreamNoInit().add(edge);
				node.getUpstreamNoInit().add(edge);
			}
		}
	}
	
	class ControlWrapper extends org.biopax.paxtools.causality.wrapper.ControlWrapper
	{
		protected ControlWrapper(Control ctrl, org.biopax.paxtools.causality.wrapper.Graph graph)
		{
			super(ctrl, graph);
		}

		@Override
		/**
		 * This function is disabled. We disconnect controls from their controllers.
		 */
		public void initUpstream()
		{
		}
	}
}
