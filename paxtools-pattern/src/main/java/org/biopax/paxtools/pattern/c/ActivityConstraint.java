package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;

/**
 * @author Ozgun Babur
 */
public class ActivityConstraint extends ConstraintAdapter
{
	boolean active;

	public ActivityConstraint(boolean active)
	{
		this.active = active;
	}

	@Override
	public int getVariableSize()
	{
		return 1;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);

		return pe.getControllerOf().isEmpty() == active;
	}
}
