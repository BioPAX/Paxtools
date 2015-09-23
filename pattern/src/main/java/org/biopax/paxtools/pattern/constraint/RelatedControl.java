package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

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
	PathAccessor controlledOf = new PathAccessor("Conversion/controlledOf*:Control");

	/**
	 * Constructor with the relation type between PhysicalEntity and Conversion.
	 * @param peType relation type between PhysicalEntity and Conversion
	 */
	public RelatedControl(RelType peType)
	{
		this(peType, null);
	}

	/**
	 * Constructor with the relation type between PhysicalEntity and Conversion, and the blacklist.
	 * @param peType relation type between PhysicalEntity and Conversion
	 * @param blacklist to detect ubiquitous small molecules
	 */
	public RelatedControl(RelType peType, Blacklist blacklist)
	{
		super(3, blacklist);
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

		if (!((peType == RelType.INPUT && getConvParticipants(conv, RelType.INPUT).contains(pe)) ||
			(peType == RelType.OUTPUT && getConvParticipants(conv, RelType.OUTPUT).contains(pe))))
			throw new IllegalArgumentException("peType = " + peType +
				", and related participant set does not contain this PE. Conv dir = " +
				getDirection(conv) + " conv.id=" + conv.getUri() + " pe.id=" +pe.getUri());

		boolean rightContains = conv.getRight().contains(pe);
		boolean leftContains = conv.getLeft().contains(pe);
		
		assert rightContains || leftContains : "PE is not a participant.";

		Set<BioPAXElement> result = new HashSet<BioPAXElement>();

		ConversionDirectionType avoidDir = (leftContains && rightContains) ? null : peType == RelType.OUTPUT ?
			(leftContains ? ConversionDirectionType.LEFT_TO_RIGHT : ConversionDirectionType.RIGHT_TO_LEFT) :
			(rightContains ? ConversionDirectionType.LEFT_TO_RIGHT : ConversionDirectionType.RIGHT_TO_LEFT);

		for (Object o : controlledOf.getValueFromBean(conv))
		{
			Control ctrl = (Control) o;
			ConversionDirectionType dir = getDirection(conv, ctrl);

			if (avoidDir != null && dir == avoidDir) continue;

			// don't collect this if the pe is ubique in the context of this control
			if (blacklist != null && blacklist.isUbique(pe, conv, dir, peType)) continue;

			result.add(ctrl);
		}
		return result;
	}

}
