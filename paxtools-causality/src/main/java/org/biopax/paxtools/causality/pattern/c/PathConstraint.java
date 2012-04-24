package org.biopax.paxtools.causality.pattern.c;

import org.biopax.paxtools.causality.pattern.Match;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class PathConstraint extends ConstraintAdapter
{
	PathAccessor pa;

	public PathConstraint(String path)
	{
		this.pa = new PathAccessor(path, BioPAXLevel.L3);
	}

	@Override
	public int getVariableSize()
	{
		return 2;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		BioPAXElement ele0 = match.get(ind[0]);
		BioPAXElement ele1 = match.get(ind[1]);

		if (ele1 == null) return false;

		Set vals = pa.getValueFromBean(ele0);
		return vals.contains(ele1);
	}

	@Override
	public boolean canGenerate()
	{
		return true;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int ... ind)
	{
		BioPAXElement ele0 = match.get(ind[0]);

		if (ele0 == null)
			throw new RuntimeException("Constraint cannot generate based on null value");

		Set vals = pa.getValueFromBean(ele0);
		List<BioPAXElement> list = new ArrayList<BioPAXElement>(vals.size());

		for (Object o : vals)
		{
			assert o instanceof BioPAXElement;
			list.add((BioPAXElement) o);
		}
		return list;
	}
}
