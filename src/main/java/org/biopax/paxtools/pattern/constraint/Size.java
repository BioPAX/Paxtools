package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;

/**
 * Checks the size of the generated elements of the wrapped constraint.
 *
 * @author Ozgun Babur
 */
public class Size extends ConstraintAdapter
{
	/**
	 * Wrapped generative constraint.
	 */
	Constraint con;

	/**
	 * Size threshold.
	 */
	int size;

	/**
	 * Type of (in)equality.
	 */
	Type type;

	/**
	 * Constructor with parameters.
	 * @param con wrapped generative constraint
	 * @param size size threshold
	 * @param type type of (in)equality
	 */
	public Size(Constraint con, int size, Type type)
	{
		if (!con.canGenerate()) throw new IllegalArgumentException(
			"The parameter constraint have to be generative.");
		
		this.con = con;
		this.size = size;
		this.type = type;
	}

	/**
	 * Size is one less than the size of wrapped constraint.
	 * @return one less than the size of wrapped constraint
	 */
	@Override
	public int getVariableSize()
	{
		return con.getVariableSize() - 1;
	}

	/**
	 * Checks if generated element size is in limits.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if generated element size is in limits
	 */
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

	/**
	 * Type of the (in)equality.
	 */
	public enum Type
	{
		EQUAL,
		GREATER,
		LESS,
		GREATER_OR_EQUAL,
		LESS_OR_EQUAL
	}
}
