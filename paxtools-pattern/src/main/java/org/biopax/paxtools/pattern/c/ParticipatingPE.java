package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.Match;

import java.util.*;

/**
 * This constraint is useful when we want traverse Control-Conversion-PE (input or output). Control
 * and Conversion are prerequisites, PE is generated.
 * @author Ozgun Babur
 */
public class ParticipatingPE extends ConstraintAdapter
{
	RelType type;

	public ParticipatingPE(RelType type)
	{
		this.type = type;
	}

	@Override
	public int getVariableSize()
	{
		return 3;
	}

	@Override
	public boolean canGenerate()
	{
		return true;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Control cont = (Control) match.get(ind[0]);
		Conversion conv = (Conversion) match.get(ind[1]);

		// This is the direction for our use only.
		ConversionDirectionType dir = null;

		for (Control c : getControlChain(cont, conv))
		{
			dir = getCatalysisDirection(c);
			if (dir != null) break;
		}
		
		if (dir == null) dir = conv.getConversionDirection();

		// No evidence for direction. Assuming LEFT_TO_RIGHT. 
		if (dir == null) dir = ConversionDirectionType.LEFT_TO_RIGHT;

		if (dir == ConversionDirectionType.LEFT_TO_RIGHT)
		{
			return new HashSet<BioPAXElement>(type == RelType.INPUT ? conv.getLeft() : conv.getRight());
		}
		else if (dir == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			return new HashSet<BioPAXElement>(type == RelType.OUTPUT ? conv.getLeft() : conv.getRight());
		}
		else
		{
			Set<BioPAXElement> result = new HashSet<BioPAXElement>(conv.getLeft());
			result.addAll(conv.getRight());
			return result;
		}
	}
}
