package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Ozgun Babur
 */
public class ConversionWrapper extends AbstractNode
{
	private Conversion conv;
	private boolean direction;
	private ConversionWrapper reverse;

	protected ConversionWrapper(Conversion conv, GraphL3 graph)
	{
		super(graph);
		this.conv = conv;
	}

	public boolean isBreadthNode()
	{
		return false;
	}

	public int getSign()
	{
		return POSITIVE;
	}

	public boolean getDirection()
	{
		return direction;
	}

	public ConversionWrapper getReverse()
	{
		return reverse;
	}

	public void init()
	{
		if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE)
		{
			reverse = new ConversionWrapper(conv, (GraphL3) graph);
			this.direction = LEFT_TO_RIGHT;
			reverse.direction = RIGHT_TO_LEFT;
			reverse.reverse = this;
		}
		else if (conv.getConversionDirection() == ConversionDirectionType.LEFT_TO_RIGHT)
		{
			this.direction = LEFT_TO_RIGHT;
		}
		else
		{
			this.direction = LEFT_TO_RIGHT;
		}

		wrapAround();

		if (reverse !=  null)
		{
			reverse.wrapAround();
		}
	}

	protected void wrapAround()
	{
		GraphL3 graph = (GraphL3) getGraph();

		for (PhysicalEntity pe : conv.getLeft())
		{
			if (direction == LEFT_TO_RIGHT) addToUpstream(pe, graph);
			else addToDownstream(pe, graph);
		}
		for (PhysicalEntity pe : conv.getRight())
		{
			if (direction == RIGHT_TO_LEFT) addToUpstream(pe, graph);
			else addToDownstream(pe, graph);
		}
		for (Control cont : conv.getControlledOf())
		{
			if (cont instanceof Catalysis)
			{
				Catalysis cat = (Catalysis) cont;

				if ((cat.getCatalysisDirection() == CatalysisDirectionType.LEFT_TO_RIGHT && direction == RIGHT_TO_LEFT) ||
					(cat.getCatalysisDirection() == CatalysisDirectionType.RIGHT_TO_LEFT && direction == LEFT_TO_RIGHT))
				{
					continue;
				}
			}

			addToUpstream(cont, graph);
		}
	}

	protected void addToUpstream(BioPAXElement ele, GraphL3 graph)
	{
		Node node = (Node) graph.getGraphObject(ele);
		Edge edge = new EdgeL3(node, this, graph);

		if (node instanceof PhysicalEntityWrapper)
		{
			((PhysicalEntityWrapper) node).getDownstreamNoInit().add(edge);
		}
		else node.getDownstream().add(edge);

		this.getUpstream().add(edge);
	}

	protected void addToDownstream(PhysicalEntity pe, GraphL3 graph)
	{
		Node node = (Node) graph.getGraphObject(pe);
		Edge edge = new EdgeL3(this, node, graph);

		if (node instanceof PhysicalEntityWrapper)
		{
			((PhysicalEntityWrapper) node).getUpstreamNoInit().add(edge);
		}
		else node.getUpstream().add(edge);


		this.getDownstream().add(edge);
	}


	public String getKey()
	{
		return conv.getRDFId() + "|" + direction;
	}

	public Conversion getConversion()
	{
		return conv;
	}

	@Override
	public Collection<Node> getUpperEquivalent()
	{
		return Collections.emptySet();
	}

	@Override
	public Collection<Node> getLowerEquivalent()
	{
		return Collections.emptySet();
	}

	public static final boolean LEFT_TO_RIGHT = true;
	public static final boolean RIGHT_TO_LEFT = false;
}
