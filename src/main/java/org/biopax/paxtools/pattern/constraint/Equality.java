package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.Match;

/**
 * Checks identity of two elements.
 *
 * @author Ozgun Babur
 */
public class Equality extends ConstraintAdapter
{
	/**
	 * Desired output.
	 */
	private boolean equals;

	/**
	 * Constructor with the desired output.
	 * @param equals the desired output
	 */
	public Equality(boolean equals)
	{
		this.equals = equals;
	}

	/**
	 * Checks if the two elements are identical or not identical as desired.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if identity checks equals the desired value
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assert ind.length == 2;

		return (match.get(ind[0]) == match.get(ind[1])) == equals;
	}

	/**
	 * This constraint checks two elements.
	 * @return 2
	 */
	@Override
	public int getVariableSize()
	{
		return 2;
	}
}
