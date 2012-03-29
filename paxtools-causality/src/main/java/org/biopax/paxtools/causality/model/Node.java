package org.biopax.paxtools.causality.model;

import java.util.Set;

/**
 * @author Ozgun Babur
 */
public interface Node extends org.biopax.paxtools.query.model.Node
{
	/**
	 * Sometimes traversing a node requires not to traverse some other nodes. For instance if we are
	 * building a path, and traversing an inactivation reaction of entity A, then the path should
	 * not grow with any activation reaction of A.
	 * @return set of banned nodes after traversing this node
	 */
	public Set<org.biopax.paxtools.query.model.Node> getBanned();

	/**
	 * Sign of current path of traversal.
	 * @return
	 */
	public int getPathSign();

	/**
	 * Sign of current path of traversal.
	 * @return
	 */
	public void setPathSign(int pathSign);


	public AlterationPack getAlterations();
}
