package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;

/**
 * @author Ozgun Babur
 */
public class Size extends ConstraintAdapter
{
	Constraint con;
	int size;
	Type type;

	public Size(Constraint con, int size, Type type)
	{
		if (!con.canGenerate()) throw new IllegalArgumentException(
			"The parameter constraint have to be generative.");
		
		this.con = con;
		this.size = size;
		this.type = type;
	}

	@Override
	public int getVariableSize()
	{
		return con.getVariableSize() - 1;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		Collection<BioPAXElement> set = con.generate(match, ind);
		
		switch (type)
		{
			case EQUAL: return set.size() == size;
			case GREATER: return set.size() > size;
			case GREATER_OR_EQUAL: return set.size() >= size;
			case LESS: return set.size() < size;
			case LESS_OR_EQUAL: return set.size() <= size;
			default: throw new RuntimeException(
				"Should not reach here. Did somebody modify Type enum?");
		}
	}

	public enum Type
	{
		EQUAL,
		GREATER,
		LESS,
		GREATER_OR_EQUAL,
		LESS_OR_EQUAL
	}
}
