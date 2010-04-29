package org.biopax.paxtools.query.model;

/**
 * @author Ozgun Babur
 */
public interface Edge extends GraphObject
{
	public Node getTargetNode();

	public Node getSourceNode();
}
