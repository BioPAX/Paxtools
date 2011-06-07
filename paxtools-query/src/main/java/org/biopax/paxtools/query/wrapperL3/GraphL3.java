package org.biopax.paxtools.query.wrapperL3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.model.AbstractGraph;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.*;


public class GraphL3 extends AbstractGraph 
{
	private Model model;

	protected final Log log = LogFactory.getLog(GraphL3.class);

	public GraphL3(Model model)
	{
		assert model.getLevel() == BioPAXLevel.L3;
		this.model = model;
	}

	@Override
	public Node wrap(Object obj)
	{
		if (obj instanceof PhysicalEntity)
		{
			return new PhysicalEntityWrapper((PhysicalEntity) obj, this);
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

	@Override
	public String getKey(Object wrapped)
	{
		if (wrapped instanceof BioPAXElement)
		{
			return ((BioPAXElement) wrapped).getRDFId();
		}

		throw new IllegalArgumentException("Object cannot be wrapped: " + wrapped);
	}

	public Set<PhysicalEntity> getPhysical(Set<EntityReference> refs)
	{
		Set<PhysicalEntity> set = new HashSet<PhysicalEntity>();

		for (EntityReference ref : refs)
		{
			set.addAll(ref.getEntityReferenceOf());
		}
		return set;
	}

	public Set<Node> getWrapperSet(Set<?> objects)
	{
		Set<Node> wrapped = new HashSet<Node>();

		for (Object object : objects)
		{
			Node node = (Node) getGraphObject(object);
			if (node != null)
			{
				wrapped.add(node);
			}
		}
		return wrapped;
	}

	@Override
	public Map<Object, Node> getWrapperMap(Set<?> objects)
	{
		Map<Object, Node> map = new HashMap<Object, Node>();

		for (Object object : objects)
		{
			Node node = (Node) getGraphObject(object);
			if (node != null)
			{
				map.put(object, node);
			}
		}
		return map;
	}

	public Set<Object> getWrappedSet(Set<? extends GraphObject> wrappers)
	{
		Set<Object> objects = new HashSet<Object>();

		for (GraphObject wrapper : wrappers)
		{
			if (wrapper instanceof PhysicalEntityWrapper)
			{
				objects.add(((PhysicalEntityWrapper) wrapper).getPhysicalEntity());
			}
			else if (wrapper instanceof ConversionWrapper)
			{
				objects.add(((ConversionWrapper) wrapper).getConversion());
			}
			else if (wrapper instanceof ControlWrapper)
			{
				objects.add(((ControlWrapper) wrapper).getControl());
			}
		}
		return objects;
	}
}
