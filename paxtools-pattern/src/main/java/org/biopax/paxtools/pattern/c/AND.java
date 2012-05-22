package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Ozgun Babur
 */
public class AND extends OR
{
	public AND(MappedConst... con)
	{
		super(con);
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		for (MappedConst mc : con)
		{
			if (!mc.getConstr().satisfies(match, translate(mc.getInds(), ind))) return false;
		}
		return true;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> gen = new HashSet<BioPAXElement> (
			con[0].getConstr().generate(match, translate(con[0].getInds(), ind)));

		for (int i = 1; i < con.length; i++)
		{
			if (gen.isEmpty()) break;

			gen.retainAll(con[i].getConstr().generate(match, translate(con[i].getInds(), ind)));
		}
		return gen;
	}
}
