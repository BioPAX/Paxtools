package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;

/**
 * Checks if the PhysicalEntity controls anything.
 *
 * @author Ozgun Babur
 */
public class ActivityConstraint extends ConstraintAdapter
{
	/**
	 * Desired activity.
	 */
	boolean active;

	/**
	 * Constructor with the desired activity.
	 * @param active desires activity
	 */
	public ActivityConstraint(boolean active)
	{
		this.active = active;
	}

	/**
	 * This is a point constraint
	 * @return 1
	 */
	@Override
	public int getVariableSize()
	{
		return 1;
	}

	/**
	 * Checks if the PhysicalEntity controls anything.
	 * @param match current match to validate
	 * @param ind mapped index
	 * @return true if it controls anything
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);

		return pe.getControllerOf().isEmpty() == active;
	}
}
