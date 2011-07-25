package org.biopax.paxtools.io.sif;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;


public class MergingSet extends AbstractSet<SimpleInteraction>
{
	HashMap<SimpleInteraction, SimpleInteraction> inner = new HashMap<SimpleInteraction, SimpleInteraction>();

	@Override public Iterator<SimpleInteraction> iterator()
	{
		return inner.keySet().iterator();
	}

	@Override public int size()
	{
		return inner.keySet().size();
	}

	@Override public boolean add(SimpleInteraction simpleInteraction)
	{

		SimpleInteraction existing = inner.remove(simpleInteraction);
		if (existing == null) existing = simpleInteraction;
		else
		{
			existing.getMediators().addAll(simpleInteraction.getMediators());
		}
		inner.put(existing, existing);
		return true;
	}
}
