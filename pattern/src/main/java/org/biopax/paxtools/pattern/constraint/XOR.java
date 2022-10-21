package org.biopax.paxtools.pattern.constraint;

import org.apache.commons.collections15.SetUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Used for getting logical XOR of a set of constraints.
 *
 * @author Ozgun Babur
 */
public class XOR extends OR
{
	/**
	 * Constructor with the mapped constraints.
	 * @param con mapped constraints
	 */
	public XOR(MappedConst... con)
	{
		super(con);
	}

	/**
	 * Checks if constraints satisfy in xor pattern.
	 * @param match match to validate
	 * @param ind mapped indices
	 * @return true if all satisfy
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		int x = -1;
		for (MappedConst mc : con)
		{
			if (mc.satisfies(match, ind)) x *= -1;
		}
		return x == 1;
	}

	/**
	 * Gets xor of the generated elements by the member constraints.
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

			Collection<BioPAXElement> subset = con[i].generate(match, ind);
			Set<BioPAXElement> copy = new HashSet<>(subset);

			copy.removeAll(gen);
			gen.removeAll(subset);
			gen.addAll(copy);
		}
		return gen;
	}
}
