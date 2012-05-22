package org.biopax.paxtools.causality;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBox;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.query.algorithm.BFS;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;
import org.biopax.paxtools.query.wrapperL3.*;

import java.util.*;

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
	public Map<Conversion, Integer> label(EntityReference er, Model model, Set<String> ubiq,
		Map<EntityReference, Set<ModificationFeature>> activating,
		Map<EntityReference, Set<ModificationFeature>> inhibiting)
	{
		Map<Conversion, Integer> convMap = new HashMap<Conversion, Integer>();

		Set<PhysicalEntity> active = getActiveStates(er);

		Set<Conversion> modifConv = getModifierConversions(er);

//		System.out.println("modifConv.size() = " + modifConv.size());
//		for (Conversion conv : modifConv)
//		{
//			System.out.println("conv.getRDFId() = " + conv.getRDFId());
//		}

		// If the PE is produced, then Conversion is activating. If it is consumed, then Conversion
		// is inhibiting.
		
		for (Conversion conv : modifConv)
		{
			int type = 0;
			
			if (conv.getLeft().isEmpty())
			{
				if (conv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT)
				{
					type = -1;
				}
				else type = 1;
			}
			else if (conv.getRight().isEmpty())
			{
				if (conv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT)
				{
					type = 1;
				}
				else type = -1;
			}
			
			if (type != 0)
			{
				convMap.put(conv, type);
			}
		}

		// Infer Conversion activity from the feature changes.

		List<Match> matches = Searcher.search(er, PatternBox.actChange(true, activating, inhibiting));

		for (Match match : matches)
		{
			Conversion conv = (Conversion) match.get(4);
			assert modifConv.contains(conv);

			if (!convMap.containsKey(conv)) convMap.put(conv, 1);
		}

		matches = Searcher.search(er, PatternBox.actChange(false, activating, inhibiting));

		for (Match match : matches)
		{
			Conversion conv = (Conversion) match.get(4);
			assert modifConv.contains(conv);

			if (!convMap.containsKey(conv)) convMap.put(conv, -1);
		}

		enrichActiveStatesWithPredicted(active, convMap);

		Graph graph = new Graph(model, ubiq, modifConv);

		Set<Node> source = new HashSet<Node>();

		for (PhysicalEntity pe : active)
		{
			source.add((Node) graph.getGraphObject(pe));
		}

		BFS bfs = new BFS(source, null, Direction.UPSTREAM, 10);

		Map<GraphObject, Integer> map = bfs.run();

		for (GraphObject go : map.keySet())
		{
			if (go instanceof ConversionWrapper)
			{
				ConversionWrapper w = (ConversionWrapper) go;
				Conversion conv = w.getConversion();
				if (!convMap.containsKey(conv)) convMap.put(conv, decideClassOfConversion(conv, map, graph));
			}
		}
		return convMap;
	}

	private void enrichActiveStatesWithPredicted(Set<PhysicalEntity> active, Map<Conversion, Integer> convMap)
	{
		// This method was supposed to add predicted active states among the active state set, but 
		// I realized this is dangerous because it can disturb the BFS labeling if the predicted
		// molecule is at the upstream of the active one.

//		for (Conversion cnv : convMap.keySet())
//		{			
//		}
	}

	private Set<Conversion> getModifierConversions(EntityReference er)
	{
		Set<Conversion> set = new HashSet<Conversion>();
		Pattern p = PatternBox.modifierConv();
		List<Match> matches = Searcher.search(er, p);
		for (Match match : matches)
		{
			set.add((Conversion) match.get(4));
		}
		return set;
	}
	
	private Integer decideClassOfConversion(Conversion cnv, Map<GraphObject, Integer> map,
		Graph graph)
	{
		Integer left = getMinLabel(cnv.getLeft(), map, graph);
		Integer right = getMinLabel(cnv.getRight(), map, graph);

		if (left.equals(right)) return 0;

		if (cnv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			return right > left ? 1 : -1;
		}
		else // treat reversible as left-to-right
		{
			return left > right ? 1 : -1;
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

	public Set<PhysicalEntity> getActiveStates(EntityReference er)
	{
		Set<PhysicalEntity> active = new HashSet<PhysicalEntity>();

		Pattern p = PatternBox.hasNonSelfEffect();
		
		for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
		{
			if (!Searcher.search(spe, p).isEmpty())
			{
				getActiveStatesRecursive(spe, active);
			}
		}
		return active;
	}

	private void getActiveStatesRecursive(PhysicalEntity pe, Set<PhysicalEntity> active)
	{
		if (!pe.getControllerOf().isEmpty() || hasActiveLabel(pe))
		{
			active.add(pe);
		}

		for (Complex cmp : pe.getComponentOf())
		{
			getActiveStatesRecursive(cmp, active);
		}
	}

	private static PathAccessor paModif = new PathAccessor("PhysicalEntity/feature:ModificationFeature/modificationType/term");
	private boolean hasActiveLabel(PhysicalEntity pe)
	{
		if (paModif.getValueFromBean(pe).contains("residue modification, active")) return true;
		
		if (stringContainsActive(pe.getDisplayName()) || stringContainsActive(pe.getStandardName())) return true;

		for (String name : pe.getName())
		{
			if (stringContainsActive(name)) return true;
		}
		return false;
	}
	
	private boolean stringContainsActive(String s)
	{
		if (s == null) return false;
		s = s.toLowerCase();
		return s.contains("activ") && !s.contains("inactiv");
	}

	private boolean stringContainsInactive(String s)
	{
		if (s == null) return false;
		s = s.toLowerCase();
		return s.contains("inactiv");
	}

	private boolean hasInactiveLabel(PhysicalEntity pe)
	{
		if (paModif.getValueFromBean(pe).contains("residue modification, inactive")) return true;

		if (stringContainsInactive(pe.getDisplayName()) || stringContainsInactive(pe.getStandardName())) return true;

		for (String name : pe.getName())
		{
			if (stringContainsInactive(name)) return true;
		}
		return false;
	}

	private static PathAccessor paComp = new PathAccessor("PhysicalEntity/componentOf*");
	private void enrichActiveStatesWithComplexes(Set<PhysicalEntity> active)
	{
		for (PhysicalEntity pe : new HashSet<PhysicalEntity>(active))
		{
			if (hasActiveLabel(pe))
			{
				for (Object o : paComp.getValueFromBean(pe))
				{
					Complex comp = (Complex) o;
					if (!comp.getControllerOf().isEmpty() || !hasInactiveMember(comp))
					{
						active.add(comp);
					}
				}
			}
		}
	}

	private static PathAccessor paMem = new PathAccessor("Complex/component*");
	private boolean hasInactiveMember(Complex comp)
	{
		for (Object o : paMem.getValueFromBean(comp))
		{
			PhysicalEntity pe = (PhysicalEntity) o;
			if (hasInactiveLabel(pe)) return true;
		}
		return false;
	}

	//---- Modified wrappers for the modified BFS labeling ----------------------------------------|
	
	class Graph extends GraphL3
	{
		Set<Conversion> modifierConv;

		public Graph(Model model, Set<String> ubiqueIDs, Set<Conversion> modifierConv)
		{
			super(model, ubiqueIDs);
			this.modifierConv = modifierConv;
		}

		public Set<Conversion> getModifierConv()
		{
			return modifierConv;
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
	
	class PhysicalEntityWrapper extends org.biopax.paxtools.query.wrapperL3.PhysicalEntityWrapper
	{
		public PhysicalEntityWrapper(PhysicalEntity pe, GraphL3 graph)
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

					if (!((Graph) getGraph()).getModifierConv().contains(conv)) continue;

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

		@Override
		public void initUpstream()
		{
			for (Conversion conv : getUpstreamConversions(pe.getParticipantOf()))
			{
				if (!((Graph) getGraph()).getModifierConv().contains(conv)) continue;

				ConversionWrapper conW = (ConversionWrapper) graph.getGraphObject(conv);
				if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
					conv.getLeft().contains(pe))
				{
					conW = conW.getReverse();
				}
				Edge edge = new EdgeL3(conW, this, graph);
				conW.getDownstreamNoInit().add(edge);
				this.getUpstreamNoInit().add(edge);
			}
		}
	}
	
	class ControlWrapper extends org.biopax.paxtools.query.wrapperL3.ControlWrapper
	{
		protected ControlWrapper(Control ctrl, GraphL3 graph)
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

		@Override
		/**
		 * This function is disabled. We disconnect controls from their controllers.
		 */
		public void initDownstream()
		{
		}
	}
}
