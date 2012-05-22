package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.pattern.Match;

import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ModificationConstraint extends ConstraintAdapter
{
	Set<String> modifications;

	static PathAccessor pa = new PathAccessor("PhysicalEntity/feature:ModificationFeature/modificationType/term");

	public ModificationConstraint(Set<String> modifications)
	{
		this.modifications = modifications;
	}

	@Override
	public int getVariableSize()
	{
		return 1;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assertIndLength(ind);

		for (Object o : pa.getValueFromBean(match.get(ind[0])))
		{
			if (modifications.contains(o.toString())) return true;
		}
		return false;
	}
}
