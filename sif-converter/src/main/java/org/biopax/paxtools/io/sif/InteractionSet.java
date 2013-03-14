package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.util.AccessibleSet;

/**
 * This is a set of binary interactions. When a new interactions is added, if the pair already
 * exists, only the mediators are updated with new information.
 */
public class InteractionSet extends AccessibleSet<SimpleInteraction>
{
	/**
	 * If the interaction already exists, mediators are updated with merging.
	 * @param simpleInteraction interaction to add
	 * @return always true
	 */
	@Override public boolean add(SimpleInteraction simpleInteraction)
	{

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
