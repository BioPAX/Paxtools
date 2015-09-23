package org.biopax.paxtools.query.wrapperL3undirected;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.Collections;

/**
 * Wrapper for the Control class.
 *
 * @author Ozgun Babur
 */
public class ControlWrapper extends AbstractNode
{
	/**
	 * Wrapped control.
	 */
	protected Control ctrl;

	/**
	 * Sign of the control.
	 */
	protected int sign;

	/**
	 * Flag to indicate if this control is related to a transcription.
	 */
	protected boolean transcription;

	/**
	 * Constructor with the Control and the owner graph.
	 * @param ctrl Control to be wrapped
	 * @param graph Owner graph
	 */
	protected ControlWrapper(Control ctrl, Graph graph)
	{
		super(graph);
		this.ctrl = ctrl;
		this.transcription = false;
	}

	/**
	 * Control is not a breadth node.
	 * @return False
	 */
	public boolean isBreadthNode()
	{
		return false;
	}

	/**
	 * @return Sign of the Control
	 */
	public int getSign()
	{
		return sign;
	}

	/**
	 * Controls are not ubiquitous molecules.
	 * @return False
	 */
	public boolean isUbique()
	{
		return false;
	}

	/**
	 * RDF ID of the Control is its key.
	 * @return Key
	 */
	public String getKey()
	{
		return ctrl.getUri();
	}

	/**
	 * Extracts the sign and the type of the Control.
	 */
	@Override
	public void init()
	{
		ControlType type = ctrl.getControlType();

		if (type != null && type.toString().startsWith("I"))
		{
			sign = NEGATIVE;
		}
		else
		{
			sign = POSITIVE;
		}

		if (ctrl instanceof TemplateReactionRegulation)
			transcription = true;
	}

	/**
	 * Puts the wrapper of the parameter element to the upstream of this Control.
	 * @param element to put at upstream
	 */
	private void bindUpstream(BioPAXElement element)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(element);

		if (node != null)
		{
			Edge edge = new EdgeL3(node, this, graph);
			node.getDownstreamNoInit().add(edge);
			this.getUpstreamNoInit().add(edge);
		}
	}

	/**
	 * Puts the wrapper of the parameter element to the upstream of this Control.
	 * @param element to put at upstream
	 */
	private void bindDownstream(BioPAXElement element)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(element);

		if (node != null)
		{
			Edge edge = new EdgeL3(this, node, graph);
			this.getDownstreamNoInit().add(edge);
			node.getUpstreamNoInit().add(edge);
		}
	}

	/**
	 * Binds the controller and other Controls that controls this control.
	 */
	@Override
	public void initUpstream()
	{
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

		for (Process prc : ctrl.getControlled())
		{
			if (prc instanceof Interaction)
			{
				bindUpstream(prc);
			}
		}
	}

	/**
	 * Binds the controlled objects.
	 */
	@Override
	public void initDownstream()
	{
		for (Controller controller : ctrl.getController())
		{
			if (controller instanceof Pathway) continue;

			PhysicalEntity pe = (PhysicalEntity) controller;
			bindDownstream(pe);
		}

		for (Control control : ctrl.getControlledOf())
		{
			bindDownstream(control);
		}

		for (Process prc : ctrl.getControlled())
		{
			if (prc instanceof Interaction)
			{
				bindDownstream(prc);
			}
		}
	}

	/**
	 * Gets the wrapped Control.
	 * @return The Control
	 */
	public Control getControl()
	{
		return ctrl;
	}

	/**
	 * Control cannot have an equivalent.
	 * @return Empty set
	 */
	@Override
	public Collection<Node> getUpperEquivalent()
	{
		return Collections.emptySet();
	}

	/**
	 * Control cannot have an equivalent.
	 * @return Empty set
	 */
	@Override
	public Collection<Node> getLowerEquivalent()
	{
		return Collections.emptySet();
	}

	/**
	 * @return whether this Control is related to a transcription event
	 */
	public boolean isTranscription()
	{
		return transcription;
	}

	/**
	 * Make or not make this control related to a transcription.
	 * @param transcription whether this Control is related to a transcription
	 */
	public void setTranscription(boolean transcription)
	{
		this.transcription = transcription;
	}
}
