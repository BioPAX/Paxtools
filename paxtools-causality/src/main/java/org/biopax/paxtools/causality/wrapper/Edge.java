package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.query.model.*;
import org.biopax.paxtools.query.wrapperL3.EdgeL3;

/**
 * @author Ozgun Babur
 */
public class Edge extends EdgeL3
{
	private int sign;
	
	public Edge(Node source, Node target, Graph graph)
	{
		super(source, target, graph);
		this.sign = 1;
	}

	@Override
	public int getSign()
	{
		return sign;
	}

	public void setSign(int sign)
	{
		this.sign = sign;
	}
}
