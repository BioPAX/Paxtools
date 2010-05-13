package org.biopax.paxtools.query.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public abstract class AbstractGraph implements Graph
{
	private Map<String, GraphObject> objectMap;

	protected AbstractGraph()
	{
		this.objectMap = new HashMap<String, GraphObject>();
	}

	public GraphObject getGraphObject(Object obj)
	{
		String key = getKey(obj);

		if (!objectMap.containsKey(key))
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

	public GraphObject getGraphObject(String id)
	{
		return objectMap.get(id);
	}

	public Map<String, GraphObject> getObjectMap()
	{
		return objectMap;
	}

	public abstract String getKey(Object wrapped);

	public abstract Node wrap(Object obj);
}
