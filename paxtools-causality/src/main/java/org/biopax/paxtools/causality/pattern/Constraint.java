package org.biopax.paxtools.causality.pattern;

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
	 * @param match
	 * @param ind
	 * @return
	 */
	public boolean satisfies(Match match, int ... ind);

	/**
	 * Number of variables to check consistency. If this is a generative constraint, then the last
	 * variable is to be generated, and other are prerequisite.
	 *
	 * @return
	 */
	public int getVariableSize();
	
	/**
	 * Tells if this constraint is a generative constraint.
	 *
	 * @return
	 */
	public boolean canGenerate();
	
	/**
	 * Generates candidate values for the variable to be generated.
	 *
	 * @param match
	 * @param ind
	 * @return
	 */
	public Collection<BioPAXElement> generate(Match match, int ... ind);
}
