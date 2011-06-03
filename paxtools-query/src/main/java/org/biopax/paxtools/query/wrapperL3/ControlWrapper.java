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
public class ControlWrapper extends AbstractNode
{
	private Control ctrl;
	int sign;

	protected ControlWrapper(Control ctrl, Graph graph)
	{
		super(graph);
		this.ctrl = ctrl;
	}

	public boolean isBreadthNode()
	{
		return false;
	}

	public int getSign()
	{
		return sign;
	}

	public String getKey()
	{
		return ctrl.getRDFId();
	}

	@Override
	public void init()
	{
		ControlType type = ctrl.getControlType();
		if (type == ControlType.ACTIVATION ||
			type == ControlType.ACTIVATION_ALLOSTERIC ||
			type == ControlType.ACTIVATION_NONALLOSTERIC ||
			type == ControlType.ACTIVATION_UNKMECH)
		{
			sign = POSITIVE;
		}
		else
		{
			sign = NEGATIVE;
		}

		for (Controller controller : ctrl.getController())
		{
			if (controller instanceof Pathway) continue;

			PhysicalEntity pe = (PhysicalEntity) controller;
			bindUpstream(pe);
		}

		for (Control control : ctrl.getControlledOf())
		{
			bindUpstream(control);
		}
	}

	private void bindUpstream(BioPAXElement element)
	{
		Node node = (Node) graph.getGraphObject(element);

		Edge edge = new EdgeL3(node, this, graph);

		if (node instanceof PhysicalEntityWrapper)
		{
			((PhysicalEntityWrapper) node).getDownstreamNoInit().add(edge);
		}
		else node.getDownstream().add(edge);

		this.getUpstream().add(edge);
	}

	public Control getControl()
	{
		return ctrl;
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
}
