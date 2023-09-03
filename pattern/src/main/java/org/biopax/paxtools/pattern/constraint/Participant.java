package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Gets input or output participants of a Conversion.
 *
 * if NOT consider Control and NOT considerPathway (default):
 *
 * var0 is a Conversion
 * var1 is a PE
 *
 * else if considerControl and NOT considerPathway
 *
 * var0 is a Control
 * var1 is a Conversion
 * var2 is a PE
 *
 * else if NOT considerControl and considerPathway
 *
 * var0 is a Pathway
 * var1 is a Conversion
 * var2 is a PE
 *
 * else if considerControl and consider pathway
 *
 * var0 is a Pathway
 * var1 is a Control
 * var2 is a Conversion
 * var3 is a PE
 *
 * @author Ozgun Babur
 */
public class Participant extends ConstraintAdapter
{
	/**
	 * Input or output.
	 */
	RelType type;

	/**
	 * Tells if there is a Control that we should consider while navigating.
	 */
	boolean considerControl;

	/**
	 * Tells if there is a Pathway that we should consider while navigating.
	 */
	boolean considerPathway;

	/**
	 * Constructor with parameters.
	 * @param type input or output conversion
	 * @param blacklist for detecting ubiquitous small molecules
	 * @param considerControl whether there is a Control that we should consider
	 * @param considerPathway whether there is a Pathway that we should consider
	 */
	public Participant(RelType type, Blacklist blacklist, boolean considerControl,
		boolean considerPathway)
	{
		super(considerControl && considerPathway ? 4 : considerControl || considerPathway ? 3 : 2,
			blacklist);
		this.type = type;
		this.considerControl = considerControl;
		this.considerPathway = considerPathway;
	}

	/**
	 * Constructor with parameters.
	 * @param type input or output of the conversion
	 */
	public Participant(RelType type)
	{
		this(type, null, false, false);
	}

	/**
	 * Constructor with parameters.
	 * @param type input or output conversion
	 * @param blacklist for detecting ubiquitous small molecules
	 */
	public Participant(RelType type, Blacklist blacklist)
	{
		this(type, blacklist, false, false);
	}

	/**
	 * Constructor with parameters.
	 * @param type input or output conversion
	 * @param blacklist for detecting ubiquitous small molecules
	 * @param considerControl whether there is a Control that we should consider
	 */
	public Participant(RelType type, Blacklist blacklist, boolean considerControl)
	{
		this(type, blacklist, considerControl, false);
	}

	/**
	 * Constructor with parameters.
	 * @param type input or output of the conversion
	 * @param considerControl whether there is a Control that we should consider
	 *
	 */
	public Participant(RelType type, boolean considerControl)
	{
		this(type, null, considerControl, false);
	}

	/**
	 * Constructor with parameters.
	 * @param type input or output of the conversion
	 * @param considerControl whether there is a Control that we should consider
	 * @param considerPathway whether there is a Pathway that we should consider
	 *
	 */
	public Participant(RelType type, boolean considerControl, boolean considerPathway)
	{
		this(type, null, considerControl, considerPathway);
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
	 * Identifies the conversion direction and gets the related participants.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return input or output participants
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Conversion cnv = (Conversion) match.get(ind[getVariableSize() - 2]);

		ConversionDirectionType dir;

		if (considerControl && considerPathway)
		{
			Pathway pathway = (Pathway) match.get(ind[0]);
			Control control = (Control) match.get(ind[1]);
			dir = getDirection(cnv, pathway, control);
		}
		else if (considerControl)
		{
			Control control = (Control) match.get(ind[0]);
			dir = getDirection(cnv, control);
		}
		else if (considerPathway)
		{
			Pathway pathway = (Pathway) match.get(ind[0]);
			dir = getDirection(cnv, pathway);
		}
		else dir = getDirection(cnv);

		Set<Set<PhysicalEntity>> sides = new HashSet<>();

		if (dir == ConversionDirectionType.REVERSIBLE)
		{
			sides.add(cnv.getLeft());
			sides.add(cnv.getRight());
		}
		else if (dir == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			sides.add(type == RelType.INPUT ? cnv.getRight() : cnv.getLeft());
		}
		// Note that null direction is treated as if LEFT_TO_RIGHT. This is not a best
		// practice, but it is a good approximation.
		else if ((dir == ConversionDirectionType.LEFT_TO_RIGHT || dir == null))
		{
			sides.add(type == RelType.OUTPUT ? cnv.getRight() : cnv.getLeft());
		}

		Collection<BioPAXElement> result = new HashSet<>();

		if (blacklist == null)
		{
			for (Set<PhysicalEntity> side : sides)
			{
				result.addAll(side);
			}
		}
		else
		{
			// remove the blacklisted from the result
			for (Set<PhysicalEntity> side : sides)
			{
				// if direction is reversible then do not mind the context
				result.addAll(blacklist.getNonUbiques(side,
					dir == ConversionDirectionType.REVERSIBLE ? null : type));
			}
		}

		return result;
	}
}
