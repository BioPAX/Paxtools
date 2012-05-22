package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.ConversionTypeLabeler;
import org.biopax.paxtools.causality.FeatureCollector;
import org.biopax.paxtools.causality.model.AlterationProvider;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.wrapperL3.EventWrapper;
import org.biopax.paxtools.query.wrapperL3.GraphL3;
import org.hibernate.cfg.ExtendsQueueEntry;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Graph extends GraphL3
{
	protected AlterationProvider alterationProvider;
	protected Map<String, ComplexMember> memberMap;
	
	public static final String ACTIVE_STATE = "Active state ";
	public static final String INACTIVE_STATE = "Inactive state ";
	public static final String ACTIVATING_CONV = "Activating conv ";
	public static final String INACTIVATING_CONV = "Inactivating conv ";
	public static final String IS_TRANSCRIPTION = "Is Transcipription";

	public Graph(Model model, Set<String> ubiqueIDs)
	{
		super(model, ubiqueIDs);
		this.memberMap = new HashMap<String, ComplexMember>();
		configureNetworkToActivity();
	}

	@Override
	public Node wrap(Object obj)
	{
		if (obj instanceof PhysicalEntity)
		{
			PhysicalEntity pe = (PhysicalEntity) obj;
			PhysicalEntityWrapper pew = new PhysicalEntityWrapper(pe, this);

			if (ubiqueIDs != null && ubiqueIDs.contains(pe.getRDFId()))
			{
				pew.setUbique(true);
			}

			return pew;
		}
		else if (obj instanceof Conversion)
		{
			return new ConversionWrapper((Conversion) obj, this);
		}
		else if (obj instanceof TemplateReaction)
		{
			return new TemplateReactionWrapper((TemplateReaction) obj, this);
		}
		else if (obj instanceof Control)
		{
			return new ControlWrapper((Control) obj, this);
		}
		else
		{
			if (log.isWarnEnabled())
			{
				log.warn("Invalid BioPAX object to wrap as node. Ignoring: " + obj);
			}
			return null;
		}
	}

	public ComplexMember wrapMember(PhysicalEntity pe)
	{
		ComplexMember cm = new ComplexMember(pe, this);
		if (ubiqueIDs != null && ubiqueIDs.contains(pe.getRDFId()))
		{
			cm.setUbique(true);
		}
		return cm;
	}

	public ComplexMember getMember(PhysicalEntity pe)
	{
//		if (pe.getComponentOf().isEmpty()) return null;
		
		String key = getKey(pe);
		ComplexMember mem = memberMap.get(key);

		if (mem == null)
		{
			mem = wrapMember(pe);

			assert (mem != null);

			memberMap.put(key, mem);
			mem.init();
		}

		return memberMap.get(key);
	}

	public Set<Node> getAllWrappers(PhysicalEntity pe)
	{
		ComplexMember mem = getMember(pe);
		if (mem != null)
		{
			Set<Node> set = new HashSet<Node>();
			set.add(mem);
			set.add((Node) getGraphObject(pe));
			return set;
		}
		return Collections.singleton((Node) getGraphObject(pe));
	}
	
	public Set<Node> getForAll(Collection<? extends PhysicalEntity> objects)
	{
		Set<Node> set = new HashSet<Node>();
		for (PhysicalEntity pe : objects)
		{
			set.add((Node) getGraphObject(pe));
		}
		return set;
	}
	
	public AlterationProvider getAlterationProvider()
	{
		return alterationProvider;
	}

	public void setAlterationProvider(AlterationProvider alterationProvider)
	{
		this.alterationProvider = alterationProvider;
	}

	public Set<Node> getBreadthNodes()
	{
		Set<Node> nodes = new HashSet<Node>();
		for (PhysicalEntity pe : model.getObjects(PhysicalEntity.class))
		{
			nodes.add((Node) getGraphObject(pe));
		}
		return nodes;
	}
	
	public void configureNetworkToActivity()
	{
		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			Set<PhysicalEntityWrapper> active = new HashSet<PhysicalEntityWrapper>();
			Set<PhysicalEntityWrapper> inactive = new HashSet<PhysicalEntityWrapper>();
			Set<EventWrapper> activating = new HashSet<EventWrapper>();
			Set<EventWrapper> inhibiting = new HashSet<EventWrapper>();

			for (PhysicalEntity pe : getRelated(pr, ACTIVE_STATE, PhysicalEntity.class))
			{
				active.add((PhysicalEntityWrapper) getGraphObject(pe));
			}
			
			for (PhysicalEntity pe : getRelated(pr, INACTIVE_STATE, PhysicalEntity.class))
			{
				inactive.add((PhysicalEntityWrapper) getGraphObject(pe));
			}

			for (Conversion conv : getRelated(pr, ACTIVATING_CONV, Conversion.class))
			{
				activating.add((EventWrapper) getGraphObject(conv));
			}

			for (Conversion conv : getRelated(pr, INACTIVATING_CONV, Conversion.class))
			{
				inhibiting.add((EventWrapper) getGraphObject(conv));
			}

			for (PhysicalEntityWrapper pe : active)
			{
				// Create negative edges from inactivating reactions to the active states
				for (EventWrapper ev : inhibiting)
				{
					Edge edge = new Edge(ev, pe, this);
					edge.setSign(-1);
				}
				
				// Create positive edges from activating reactions to the active states
				for (EventWrapper ev: activating)
				{
					Edge edge = new Edge(ev, pe, this);
					edge.setSign(1);
				}
			}

			for (PhysicalEntityWrapper pe : inactive)
			{
				// Create positive edges from inactivating reactions to the inactive states
				for (EventWrapper ev : inhibiting)
				{
					Edge edge = new Edge(ev, pe, this);
					edge.setSign(1);
				}

				// Create negative edges from activating reactions to the inactive states
				for (EventWrapper ev: activating)
				{
					Edge edge = new Edge(ev, pe, this);
					edge.setSign(-1);
				}
			}

			// Traversing one of these activating or inactivating reactions bans to traverse others

			Set<EventWrapper> reac = new HashSet<EventWrapper>(activating);
			reac.addAll(inhibiting);

			for (EventWrapper ev : reac)
			{
				ev.initBanned();
				ev.getBanned().addAll(reac);
				ev.getBanned().remove(ev);
			}
		}
	}
	
	private <T extends BioPAXElement> Set<T> getRelated(ProteinReference pr, String tag, Class<T> c)
	{
		Set<T> set = new HashSet<T>();

		for (String com : pr.getComment())
		{
			if (com.startsWith(tag))
			{
				String id = com.substring(com.lastIndexOf(" ") + 1);
				T ele = (T) model.getByID(id);
				assert ele != null;
				set.add(ele);
			}
		}
		return set;
	}
}
