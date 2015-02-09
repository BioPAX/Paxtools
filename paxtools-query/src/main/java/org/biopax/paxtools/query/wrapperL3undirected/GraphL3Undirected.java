package org.biopax.paxtools.query.wrapperL3undirected;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.AbstractGraph;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;
import org.biopax.paxtools.query.wrapperL3.Filter;

import java.util.*;

/**
 * Wrapper for L3 Graphs.
 *
 * @author Ozgun Babur
 */
public class GraphL3Undirected extends AbstractGraph
{
	/**
	 * The model to be wrapped.
	 */
	protected Model model;

	protected List<Filter> filters;

	/**
	 * Log for logging.
	 */
	protected final Log log = LogFactory.getLog(GraphL3Undirected.class);

	/**
	 * Constructor with the model and the IDs of the ubiquitous molecules. IDs can be null, meaning
	 * no labeling is desired.
	 * @param model Model to wrap
	 * @param filters for filtering graph elements
	 */
	public GraphL3Undirected(Model model, Filter... filters)
	{
		assert model.getLevel() == BioPAXLevel.L3;
		this.model = model;

		if (filters.length > 0)
		{
			this.filters = Arrays.asList(filters);
		}
	}

	/**
	 * There must be no filter opposing to traverse this object to traverse it.
	 * @param ele element to check
	 * @return true if ok to traverse
	 */
	private boolean passesFilters(Level3Element ele)
	{
		if (filters == null) return true;

		for (Filter filter : filters)
		{
			if (!filter.okToTraverse(ele)) return false;
		}
		return true;
	}

	/**
	 * This method creates a wrapper for every wrappable L3 element.
	 * @param obj Object to wrap
	 * @return The wrapper
	 */
	@Override
	public Node wrap(Object obj)
	{
		// Check if the object is level3
		if (!(obj instanceof Level3Element)) throw new IllegalArgumentException(
			"An object other than a Level3Element is trying to be wrapped: " + obj);

		// Check if the object passes the filter
		if (!passesFilters((Level3Element) obj)) return null;

		// Wrap if traversible

		if (obj instanceof PhysicalEntity)
		{
			return new PhysicalEntityWrapper((PhysicalEntity) obj, this);
		}
		else if (obj instanceof Control)
		{
			return new ControlWrapper((Control) obj, this);
		}
		else if (obj instanceof Interaction)
		{
			return new InteractionWrapper((Interaction) obj, this);
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

	/**
	 * RDF IDs of elements is used as key in the object map.
	 * @param wrapped Object to wrap
	 * @return Key
	 */
	@Override
	public String getKey(Object wrapped)
	{
		if (wrapped instanceof BioPAXElement)
		{
			return ((BioPAXElement) wrapped).getRDFId();
		}

		throw new IllegalArgumentException("Object cannot be wrapped: " + wrapped);
	}

	/**
	 * Gets wrappers of given elements
	 * @param objects Wrapped objects
	 * @return wrappers
	 */
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

	/**
	 * Gets an element-to-wrapper map for the given elements.
	 * @param objects Wrapped objects
	 * @return object-to-wrapper map
	 */
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

	/**
	 * Gets the wrapped objects of the given wrappers.
	 * @param wrappers Wrappers
	 * @return Wrapped objects
	 */
	public Set<Object> getWrappedSet(Set<? extends GraphObject> wrappers)
	{
		Set<Object> objects = new HashSet<Object>();

		for (GraphObject wrapper : wrappers)
		{
			if (wrapper instanceof PhysicalEntityWrapper)
			{
				objects.add(((PhysicalEntityWrapper) wrapper).getPhysicalEntity());
			}
			else if (wrapper instanceof ControlWrapper)
			{
				objects.add(((ControlWrapper) wrapper).getControl());
			}
			else if (wrapper instanceof InteractionWrapper)
			{
				objects.add(((InteractionWrapper) wrapper).getInteraction());
			}
		}
		return objects;
	}

	/**
	 * @return Wrapped model
	 */
	public Model getModel()
	{
		return model;
	}
}
