package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Match;

import java.util.Set;

/**
 * Checks if the element has the desired ID. This is a separate class (not reusing FieldConstraint)
 * because PathAccessor cannot access to RDFIDs.
 *
 * @author Ozgun Babur
 */
public class IDConstraint extends ConstraintAdapter
{
	/**
	 * Desired IDs.
	 */
	Set<String> ids;

	/**
	 * Constructor with desired IDs.
	 * @param ids desired IDs for valid elements to have
	 */
	public IDConstraint(Set<String> ids)
	{
		this.ids = ids;
	}

	/**
	 * This is a point constraint.
	 * @return 1
	 */
	@Override
	public int getVariableSize()
	{
		return 1;
	}

	/**
	 * Checks if the element has one of the desired IDs.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the ID is in the list
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return ids.contains(match.get(ind[0]).getRDFId());
	}
}
