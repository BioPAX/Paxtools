package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;

import java.util.*;

/**
 * This constraint is useful when we want traverse Control-Conversion-PE (input or output). Control
 * and Conversion are prerequisites, PE is generated.
 *
 * Var0 - Control
 * Var1 - Conversion
 * Var2 - input or output PhysicalEntity
 *
 * @author Ozgun Babur
 */
public class ParticipatingPE extends ConstraintAdapter
{
	/**
	 * Desired direction after the Conversion.
	 */
	RelType type;

	/**
	 * If this is false then all participants of the Conversion is traversed.
	 */
	boolean treatReversibleAsLeftToRight;

	/**
	 * Constructor with parameters.
	 * @param type direction from the Conversion
	 * @param treatReversibleAsLeftToRight option to treat reversible Conversion as if left to right
	 */
	public ParticipatingPE(RelType type, boolean treatReversibleAsLeftToRight)
	{
		super(3);
		this.type = type;
		this.treatReversibleAsLeftToRight = treatReversibleAsLeftToRight;
	}

	/**
	 * This is a generative constraint.
	 * @return
	 */
	@Override
	public boolean canGenerate()
	{
		return true;
	}

	/**
	 * First evaluates the direction that the Control is affecting the Conversion, then gets the
	 * related participants.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return related participants
	 */
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

		if (dir == ConversionDirectionType.LEFT_TO_RIGHT ||
			(dir == ConversionDirectionType.REVERSIBLE && treatReversibleAsLeftToRight))
		{
			return new HashSet<BioPAXElement>(type == RelType.INPUT ? conv.getLeft() : conv.getRight());
		}
		else if (dir == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			return new HashSet<BioPAXElement>(type == RelType.OUTPUT ? conv.getLeft() : conv.getRight());
		}
		else // dir is reversible and we will go both sides
		{
			Set<BioPAXElement> result = new HashSet<BioPAXElement>(conv.getLeft());
			result.addAll(conv.getRight());
			return result;
		}
	}
}
