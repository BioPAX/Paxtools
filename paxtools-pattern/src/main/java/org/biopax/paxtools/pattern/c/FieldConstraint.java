package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;

/**
 * @author Ozgun Babur
 */
public class FieldConstraint extends ConstraintAdapter
{
	PathAccessor pa;
	Collection acceptedValues;

	public FieldConstraint(String pathToField, Collection acceptedValues)
	{
		pa = new PathAccessor(pathToField);
		this.acceptedValues = acceptedValues;
	}

	@Override
	public int getVariableSize()
	{
		return 1;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		BioPAXElement ele = match.get(ind[0]);

		for (Object val : pa.getValueFromBean(ele))
		{
			if (acceptedValues.contains(val)) return true;
		}
		return false;
	}
}
