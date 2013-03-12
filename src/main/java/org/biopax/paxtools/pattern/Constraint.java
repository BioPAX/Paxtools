package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;

/**
 * A constraint to check if a set of variables satisfy the requirements. If a constraint
 * canGenerate, then it can generate possible values for the last variable, using the other
 * variables.
 *
 * @author Ozgun Babur
 */
public interface Constraint
{
	/**
	 * Checks if the variables in the Match satisfies this constraint.
	 *
	 * @param match current pattern match
	 * @param ind mapped indices of the match
	 * @return true if this constraint is satisfied
	 */
	public boolean satisfies(Match match, int ... ind);

	/**
	 * Number of variables to check consistency. If this is a generative constraint, then the last
	 * variable is to be generated, and other are prerequisite.
	 *
	 * @return number of indexes this constraint uses
	 */
	public int getVariableSize();
	
	/**
	 * Tells if this constraint is a generative constraint.
	 *
	 * @return true if constraint is generative
	 */
	public boolean canGenerate();
	
	/**
	 * Generates candidate values for the variable to be generated.
	 *
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return generated values that satisfy this constraint
	 */
	public Collection<BioPAXElement> generate(Match match, int ... ind);
}
