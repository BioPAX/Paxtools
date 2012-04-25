package org.biopax.paxtools.pattern.c;

/**
 * Logical OR of several PathConstraints.
 *
 * @author Ozgun Babur
 */
public class MultiPathConstraint extends OR
{
	public MultiPathConstraint(String ... paths)
	{
		con = new PathConstraint[paths.length];

		for (int i = 0; i < con.length; i++)
		{
			con[i] = new PathConstraint(paths[i]);
		}
	}
}
