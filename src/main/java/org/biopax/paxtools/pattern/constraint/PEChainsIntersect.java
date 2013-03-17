package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.PhysicalEntityChain;

/**
 * This constraint checks if two chains of linked physical entities are intersecting or not.
 *
 * Var0 First PhysicalEntity in the first chain
 * Var1 Last PhysicalEntity in the first chain
 * Var2 First PhysicalEntity in the second chain
 * Var3 Last PhysicalEntity in the second chain
 *
 * @author Ozgun Babur
 */
public class PEChainsIntersect extends ConstraintAdapter
{
	/**
	 * Desired result.
	 */
	boolean intersectionDesired;

	/**
	 * Option to ignore intersection at the endpoints of the chains.
	 */
	boolean ignoreEndPoints;

	/**
	 * Constructor with the desired result.
	 * @param intersectionDesired desired result
	 */
	public PEChainsIntersect(boolean intersectionDesired)
	{
		this(intersectionDesired, false);
	}

	/**
	 * Constructor with the desired result and endpoint ignore option.
	 * @param intersectionDesired desired result
	 * @param ignoreEndPoints option to ignore intersection at the endpoints of the chains
	 */
	public PEChainsIntersect(boolean intersectionDesired, boolean ignoreEndPoints)
	{
		this.intersectionDesired = intersectionDesired;
		this.ignoreEndPoints = ignoreEndPoints;
	}

	/**
	 * Works with 4 elements
	 * @return 4
	 */
	@Override
	public int getVariableSize()
	{
		return 4;
	}

	/**
	 * Creates two PhysicalEntity chains with the given endpoints, and checks if they are
	 * intersecting.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the chains are intersecting or not intersecting as desired
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		PhysicalEntity pe0 = (PhysicalEntity) match.get(ind[0]);
		PhysicalEntity pe1 = (PhysicalEntity) match.get(ind[1]);
		PhysicalEntity pe2 = (PhysicalEntity) match.get(ind[2]);
		PhysicalEntity pe3 = (PhysicalEntity) match.get(ind[3]);

		PhysicalEntityChain ch1 = new PhysicalEntityChain(pe0, pe1);
		PhysicalEntityChain ch2 = new PhysicalEntityChain(pe2, pe3);

		return ch1.intersects(ch2, ignoreEndPoints) == intersectionDesired;
	}
}
