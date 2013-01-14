package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class RelatedControl extends ConstraintAdapter
{
	RelType peType;
	PathAccessor controlledOf = new PathAccessor("Control/controlledOf*:Control");

	public RelatedControl(RelType peType)
	{
		this.peType = peType;
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
