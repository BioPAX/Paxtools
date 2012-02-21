package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Graph;
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

	public boolean isUbique()
	{
		return false;
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
		if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
			this.reverse == null)
		{
			reverse = new ConversionWrapper(conv, (GraphL3) graph);
			this.direction = LEFT_TO_RIGHT;
			reverse.direction = RIGHT_TO_LEFT;
			reverse.reverse = this;
		}
		else if (conv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			this.direction = RIGHT_TO_LEFT;
		}
		else
		{
			this.direction = LEFT_TO_RIGHT;
		}
	}

	@Override
	public void initUpstream()
	{
		if (direction == LEFT_TO_RIGHT)
		{
			for (PhysicalEntity pe : conv.getLeft())
			{
				addToUpstream(pe, getGraph());
			}
		}
		else
		{
			for (PhysicalEntity pe : conv.getRight())
			{
				addToUpstream(pe, getGraph());
			}
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

	@Override
	public void initDownstream()
	{
		if (direction == RIGHT_TO_LEFT)
		{
			for (PhysicalEntity pe : conv.getLeft())
			{
				addToDownstream(pe, getGraph());
			}
		}
		else
		{
			for (PhysicalEntity pe : conv.getRight())
			{
				addToDownstream(pe, getGraph());
			}
		}
	}

	protected void addToUpstream(BioPAXElement ele, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(ele);
		Edge edge = new EdgeL3(node, this, graph);

		node.getDownstreamNoInit().add(edge);
		this.getUpstreamNoInit().add(edge);
	}

	protected void addToDownstream(PhysicalEntity pe, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(pe);
		Edge edge = new EdgeL3(this, node, graph);

		node.getUpstreamNoInit().add(edge);
		this.getDownstreamNoInit().add(edge);
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
