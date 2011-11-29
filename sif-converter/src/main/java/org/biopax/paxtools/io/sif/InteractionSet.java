package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.util.AccessibleSet;


public class InteractionSet extends AccessibleSet<SimpleInteraction>
{

	@Override public boolean add(SimpleInteraction simpleInteraction)
	{
		if(simpleInteraction.getSource().getRDFId().startsWith("http://biopax.org/generated/fixer/normalizeGenerics"))
			System.out.println("ooops");
		SimpleInteraction existing = access(simpleInteraction);
		if (existing == null)
		{
			existing = simpleInteraction;
			super.add(existing);
		}
		else
		{
			existing.getMediators().addAll(simpleInteraction.getMediators());
		}
		return true;
	}
}
