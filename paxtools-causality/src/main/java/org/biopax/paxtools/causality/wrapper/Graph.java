package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.AlterationProvider;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.model.Node;
import org.biopax.paxtools.query.wrapperL3.EventWrapper;
import org.biopax.paxtools.query.wrapperL3.GraphL3;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class Graph extends GraphL3
{
	AlterationProvider alterationProvider;

	public Graph(Model model, Set<String> ubiqueIDs)
	{
		super(model, ubiqueIDs);
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

	public AlterationProvider getAlterationProvider()
	{
		return alterationProvider;
	}

	public void setAlterationProvider(AlterationProvider alterationProvider)
	{
		this.alterationProvider = alterationProvider;
	}

	public void configureNetworkToActivity()
	{
		for (EntityReference er : model.getObjects(EntityReference.class))
		{
			Set<PhysicalEntityWrapper> active = new HashSet<PhysicalEntityWrapper>();
			Set<EventWrapper> activating = new HashSet<EventWrapper>();
			Set<EventWrapper> inhibiting = new HashSet<EventWrapper>();

			// todo: create those sets using state network analyzer


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
}
