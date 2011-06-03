package org.biopax.paxtools.query.model;

import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.query.wrapperL3.ConversionWrapper;

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
