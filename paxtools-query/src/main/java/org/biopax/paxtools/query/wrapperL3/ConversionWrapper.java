package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.level3.*;

/**
 * Wrapper for the Conversion class.
 *
 * @author Ozgun Babur
 */
public class ConversionWrapper extends EventWrapper
{
	/**
	 * Wrapped Conversion.
	 */
	protected Conversion conv;

	/**
	 * The direction that the Conversion is wrapped. A separate wrapper is used for each direction
	 * if the Conversion is reversible.
	 */
	protected boolean direction;

	/**
	 * Link to the wrapper for the same Conversion but for the other direction, if exists.
	 */
	private ConversionWrapper reverse;

	/**
	 * Flag to say this Conversion is a transcription.
	 */
	protected boolean transcription;

	/**
	 * Constructor with the Conversion to wrap and the owner graph.
	 * @param conv Conversion to wrap
	 * @param graph Owner graph
	 */
	protected ConversionWrapper(Conversion conv, GraphL3 graph)
	{
		super(graph);
		this.conv = conv;
	}

	/**
	 * @return Direction of the conversion
	 */
	public boolean getDirection()
	{
		return direction;
	}

	/**
	 * @return The reverse Conversion wrapper if exists
	 */
	public ConversionWrapper getReverse()
	{
		return reverse;
	}

	/**
	 * Extracts the direction, creates the reverse if necessary.
	 */
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

	/**
	 * Binds inputs and controllers.
	 */
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

	/**
	 * Binds products.
	 */
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

	/**
	 * @return Whether this is a transcription
	 */
	public boolean isTranscription()
	{
		return transcription;
	}

	/**
	 * RDF ID of the Conversion and the direction is used for the key.
	 * @return Key
	 */
	public String getKey()
	{
		return conv.getUri() + "|" + direction;
	}

	/**
	 * @return Wrapped Conversion
	 */
	public Conversion getConversion()
	{
		return conv;
	}

	/**
	 * @return Display name with the ID
	 */
	@Override
	public String toString()
	{
		return conv.getDisplayName() + " -- " + conv.getUri();
	}

	/**
	 * Direction LEFT_TO_RIGHT.
	 */
	public static final boolean LEFT_TO_RIGHT = true;

	/**
	 * Direction RIGHT_TO_LEFT.
	 */
	public static final boolean RIGHT_TO_LEFT = false;
}
