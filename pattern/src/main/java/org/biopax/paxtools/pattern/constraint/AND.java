package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;
import java.util.HashSet;

/**
 * Used for getting logical AND of a set of constraints.
 *
 * @author Ozgun Babur
 */
public class AND extends OR
{
	/**
	 * Constructor with the mapped constraints.
	 * @param con mapped constraints
	 */
	public AND(MappedConst... con)
	{
		super(con);
	}

	/**
	 * Checks if all the constraints satisfy.
	 * @param match match to validate
	 * @param ind mapped indices
	 * @return true if all satisfy
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		for (MappedConst mc : con)
		{
			if (!mc.satisfies(match, ind)) return false;
		}
		return true;
	}

	/**
	 * Gets intersection of the generated elements by the member constraints.
	 * @param match match to process
	 * @param ind mapped indices
	 * @return satisfying elements
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> gen = new HashSet<> (
			con[0].generate(match, ind));

		for (int i = 1; i < con.length; i++)
		{
			if (gen.isEmpty()) break;

			gen.retainAll(con[i].generate(match, ind));
		}
		return gen;
	}
}
