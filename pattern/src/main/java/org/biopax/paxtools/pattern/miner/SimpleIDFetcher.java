package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collections;
import java.util.Set;

/**
 * Simple gets the URI (of an entity reference);
 */
public class SimpleIDFetcher implements IDFetcher
{
	@Override
	public Set<String> fetchID(BioPAXElement ele)
	{
		return Collections.singleton(ele.getUri());
	}

}
