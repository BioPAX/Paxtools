package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * Checks if a variable is a specific type or its subclass.
 *
 * @author Ozgun Babur
 */
public class Type extends ConstraintAdapter
{
	/**
	 * Desired class.
	 */
	private Class<? extends BioPAXElement> clazz;

	/**
	 * Constructor with the desired class.
	 * @param clazz desired class
	 */
	public Type(Class<? extends BioPAXElement> clazz)
	{
		this.clazz = clazz;
	}

	/**
	 * Checks if the element is assignable to a variable of the desired type.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the element is assignable to a variable of the desired type
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assert ind.length == 1;

		return clazz.isAssignableFrom(match.get(ind[0]).getModelInterface());
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
}
