package org.biopax.paxtools.query.model;

/**
 * @author Ozgun Babur
 */
public interface Edge extends GraphObject
{
	Node getTargetNode();

	Node getSourceNode();

	public int getSign();
}
