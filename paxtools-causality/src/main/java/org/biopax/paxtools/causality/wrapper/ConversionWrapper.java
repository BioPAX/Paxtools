package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ConversionWrapper extends org.biopax.paxtools.query.wrapperL3.ConversionWrapper
	implements Node
{
	protected ConversionWrapper(Conversion conv, Graph graph)
	{	
		super(conv, graph);

		for (String s : conv.getComment())
		{
			if (s.equals(Graph.IS_TRANSCRIPTION))
			{
				transcription = true;
				break;
			}
		}
	}

	@Override
	public void initUpstream()
	{
		// Upstream of a conversion is not its substrates.

		// Continue from controls

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
	public AlterationPack getAlterations()
	{
		return null;
	}
}
