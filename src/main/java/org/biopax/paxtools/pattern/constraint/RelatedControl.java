package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * After traversing a PhysicalEntity and the Conversion it participates, this constraint takes us to
 * the Control. The given relation between PhysicalEntity and Conversion is used to filter out
 * unrelated controls.
 *
 * Var0 - PhysicalEntity (participant)
 * Var1 - Conversion
 * Var2 - related Control
 *
 * @author Ozgun Babur
 */
public class RelatedControl extends ConstraintAdapter
{
	/**
	 * Relation type between PhysicalEntity and Conversion.
	 */
	RelType peType;

	/**
	 * Accessor to controller of Control recursively.
	 */
	PathAccessor controlledOf = new PathAccessor("Control/controlledOf*:Control");

	/**
	 * Constructor with the relation type between PhysicalEntity and Conversion.
	 * @param peType relation type between PhysicalEntity and Conversion
	 */
	public RelatedControl(RelType peType)
	{
		super(3);
		this.peType = peType;
	}

	/**
	 * This is a generative constraint.
	 * @return true
	 */
	@Override
	public boolean canGenerate()
	{
		return true;
	}

	/**
	 * According to the relation between PhysicalEntity and the Conversion, checks of the Control is
	 * relevant. If relevant it is retrieved.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return related controls
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);
		Conversion conv = (Conversion) match.get(ind[1]);

		assert (peType == RelType.INPUT && getConvParticipants(conv, RelType.INPUT).contains(pe)) ||
			(peType == RelType.OUTPUT && getConvParticipants(conv, RelType.OUTPUT).contains(pe)) :
			"peType = " + peType + ", and related participant set does not contain this PE.";

		boolean rightContains = conv.getRight().contains(pe);
		boolean leftContains = conv.getLeft().contains(pe);
		
		assert rightContains || leftContains : "PE is not a participant.";

		Set<BioPAXElement> result = new HashSet<BioPAXElement>();

		if (leftContains && rightContains)
		{
			result.addAll(controlledOf.getValueFromBean(conv));
			return result;
		}

		CatalysisDirectionType avoidDir = peType == RelType.OUTPUT ?
			(leftContains ? CatalysisDirectionType.LEFT_TO_RIGHT : CatalysisDirectionType.RIGHT_TO_LEFT) :
			(rightContains ? CatalysisDirectionType.LEFT_TO_RIGHT : CatalysisDirectionType.RIGHT_TO_LEFT);

		for (Control ctrl : conv.getControlledOf())
		{
			if (ctrl instanceof Catalysis)
			{
				if (((Catalysis) ctrl).getCatalysisDirection() == avoidDir)
				{
					continue;
				}

				result.add(ctrl);
				result.addAll(controlledOf.getValueFromBean(conv));
			}
		}
		return result;
	}

}
