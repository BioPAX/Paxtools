package org.biopax.paxtools.causality.pattern.c;

import org.biopax.paxtools.causality.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * Checks if a variable is a specific type or its superclass.
 *
 * @author Ozgun Babur
 */
public class Type extends ConstraintAdapter
{
	private Class<? extends BioPAXElement> clazz;

	public Type(Class<? extends BioPAXElement> clazz)
	{
		this.clazz = clazz;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return match.get(ind[0]).getModelInterface().isAssignableFrom(clazz);
	}

	@Override
	public int getVariableSize()
	{
		return 1;
	}
}
