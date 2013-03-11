package org.biopax.paxtools.query.wrapperL3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.AbstractGraph;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.*;

/**
 * Wrapper for L3 Graphs.
 *
 * @author Ozgun Babur
 */
public class GraphL3 extends AbstractGraph 
{
	/**
	 * The model to be wrapped.
	 */
	protected Model model;

	/**
	 * RDF IDs of the ubiquitous molecules.
	 */
	protected Set<String> ubiqueIDs;

	/**
	 * Log for logging.
	 */
	protected final Log log = LogFactory.getLog(GraphL3.class);

	/**
	 * Constructor with the model and the IDs of the ubiquitous molecules. IDs can be null, meaning
	 * no labeling is desired.
	 * @param model Model to wrap
	 * @param ubiqueIDs RDF IDs of ubiques
	 */
	public GraphL3(Model model, Set<String> ubiqueIDs)
	{
		assert model.getLevel() == BioPAXLevel.L3;
		this.model = model;
		setUbiqueIDs(ubiqueIDs);
	}

	/**
	 * @param ubiqueIDs IDs of ubiquitous molecules
	 */
	public void setUbiqueIDs(Set<String> ubiqueIDs)
	{
		this.ubiqueIDs = ubiqueIDs;
	}

	/**
	 * This method creates a wrapper for every wrappable L3 element.
	 * @param obj Object to wrap
	 * @return The wrapper
	 */
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
			else if (wrapper instanceof ConversionWrapper)
			{
				objects.add(((ConversionWrapper) wrapper).getConversion());
			}
			else if (wrapper instanceof TemplateReactionWrapper)
			{
				objects.add(((TemplateReactionWrapper) wrapper).getTempReac());
			}
			else if (wrapper instanceof ControlWrapper)
			{
				objects.add(((ControlWrapper) wrapper).getControl());
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
