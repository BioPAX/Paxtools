package org.biopax.paxtools.query.wrapperL3undirected;

import org.biopax.paxtools.query.model.AbstractEdge;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.Node;

/**
 * Wrapper for links between L3 objects.
 *
 * @author Ozgun Babur
 */
public class EdgeL3 extends AbstractEdge
{
	/**
	 * Flag for being related to a transcription.
	 */
	boolean transcription;

	/**
	 * Constructor with source and target nodes, and the owner graph. Sets transcription flag to
	 * False by default.
	 *
	 * @param source Source node
	 * @param target Target node
	 * @param graph Owner graph
	 */
	public EdgeL3(Node source, Node target, Graph graph)
	{
		super(source, target, graph);
		transcription = false;
	}

	/**
	 * @return True if this is related to a transcription
	 */
	public boolean isTranscription()
	{
		return transcription;
	}

	/**
	 * Set the transcription flag.
	 * @param transcription The transcription flag
	 */
	public void setTranscription(boolean transcription)
	{
		this.transcription = transcription;
	}
}
