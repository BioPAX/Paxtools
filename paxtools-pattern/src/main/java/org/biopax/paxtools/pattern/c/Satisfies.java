package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Ozgun Babur
 */
public class Satisfies extends ConstraintAdapter
{
	MappedConst mc;
	
	public Satisfies(MappedConst mc)
	{
		this.mc = mc;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return mc.satisfies(match, ind);
	}

	@Override
	public int getVariableSize()
	{
		return mc.getVariableSize();
	}
}
