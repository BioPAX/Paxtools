package org.biopax.paxtools.query.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter class for a graph that is queried.
 *
 * @author Ozgun Babur
 */
public abstract class AbstractGraph implements Graph
{
	/**
	 * Objects are stored in this map. getKey method of objects is used for generating the key.
	 */
	protected Map<String, GraphObject> objectMap;

	/**
	 * Empty constructor that initializes the object map.
	 */
	protected AbstractGraph()
	{
		this.objectMap = new HashMap<String, GraphObject>();
	}

	/**
	 * Gets the related wrapper for the given object, creates the wrapper if not created before.
	 * @param obj Object to wrap
	 * @return wrapper
	 */
	public GraphObject getGraphObject(Object obj)
	{
		String key = getKey(obj);
		GraphObject go = objectMap.get(key);

//		if (obj instanceof Conversion && go == null)
//		{
//			go = objectMap.get(key + ConversionWrapper.LEFT_TO_RIGHT);
//			if (go == null)
//				go = objectMap.get(key + ConversionWrapper.RIGHT_TO_LEFT);
//		}

		if (go == null)
		{
			Node node = wrap(obj);

			if (node != null)
			{
				objectMap.put(key, node);
				node.init();
			}
		}

		return objectMap.get(key);
	}

	/**
	 * Gets the wrapper object with its id (key).
	 * @param id Whatever getKey method return for the wrapped object.
	 * @return Wrapper
	 */
	public GraphObject getGraphObject(String id)
	{
		return objectMap.get(id);
	}

	/**
	 * @return The object map
	 */
	public Map<String, GraphObject> getObjectMap()
	{
		return objectMap;
	}

	/**
	 * Clears memory of all wrapper in the object map.
	 */
	public void clear()
	{
		for (GraphObject go : objectMap.values())
		{
			go.clear();
		}
	}

	/**
	 * @param wrapped Object to wrap
	 * @return A key for the object to map it to its wrapper
	 */
	public abstract String getKey(Object wrapped);

	/**
	 * Creates the wrapper for the given object.
	 * @param obj Object to wrap
	 * @return The wrapper
	 */
	public abstract Node wrap(Object obj);
}
