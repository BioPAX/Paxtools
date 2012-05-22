package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.level3.*;

/**
 * @author Ozgun Babur
 */
public class ConversionWrapper extends EventWrapper
{
	protected Conversion conv;
	protected boolean direction;
	private ConversionWrapper reverse;
	protected boolean transcription;

	protected ConversionWrapper(Conversion conv, GraphL3 graph)
	{
		super(graph);
		this.conv = conv;
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

	public boolean isTranscription()
	{
		return transcription;
	}

	public String getKey()
	{
		return conv.getRDFId() + "|" + direction;
	}

	public Conversion getConversion()
	{
		return conv;
	}

	public static final boolean LEFT_TO_RIGHT = true;
	public static final boolean RIGHT_TO_LEFT = false;
}
