package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.*;

/**
 * This is a base class for a Constraint. Most constraints should typically extend this class.
 *
 * @author Ozgun Babur
 */
public abstract class ConstraintAdapter implements Constraint
{
	/**
	 * Specifies if the constraint is generative. If you override this method, then don't forget to
	 * also override getGeneratedInd, and generate methods.
	 */
	@Override
	public boolean canGenerate()
	{
		return false;
	}

	/**
	 * This method has to be overridden by generative constraints.
	 *
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return elements that satisfy this constraint
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int ... ind)
	{
		throw new RuntimeException("This constraint is not generative. " +
			"Please check with canGenerate first.");
	}

	/**
	 * Use this method only if constraint canGenerate, and satisfaction criteria is that simple.
	 *
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the match satisfies this constraint
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return generate(match, ind).contains(match.get(ind[ind.length - 1]));
	}

	/**
	 * Gets the direction of the Control chain the the Interaction.
	 * @param cont top control
	 * @param inter controlled interaction
	 * @return the direction of catalysis, if any found
	 */
	protected ConversionDirectionType getCatalysisDirection(Control cont, Interaction inter)
	{
		for (Control ctrl : getControlChain(cont, inter))
		{
			ConversionDirectionType dir = getCatalysisDirection(ctrl);
			if (dir != null) return dir;
		}
		return null;
	}

	/**
	 * Gets the direction of the Control, if exists.
	 * @param cont Control to get its direction
	 * @return the direction of the Control
	 */
	protected ConversionDirectionType getCatalysisDirection(Control cont)
	{
		if (cont instanceof Catalysis)
		{
			CatalysisDirectionType catDir = ((Catalysis) cont).getCatalysisDirection();

			if (catDir == CatalysisDirectionType.LEFT_TO_RIGHT)
			{
				return ConversionDirectionType.LEFT_TO_RIGHT;
			}
			else if (catDir == CatalysisDirectionType.RIGHT_TO_LEFT)
			{
				return ConversionDirectionType.RIGHT_TO_LEFT;
			}
		}
		return null;
	}

	/**
	 * Gets the chain of Control, staring from the given Control, leading to the given Interaction.
	 * Use this method only if you are sure that there is a link from the control to conversion.
	 * Otherwise a RuntimeException is thrown.
	 *
	 * @param control top level Control
	 * @param inter target Interaction
	 * @return Control chain controlling the Interaction
	 */
	protected List<Control> getControlChain(Control control, Interaction inter)
	{
		LinkedList<Control> list = new LinkedList<Control>();
		list.add(control);

		boolean found = search(list, inter);

		if (!found) throw new RuntimeException("No link from Control to Conversion.");

		return list;
	}

	/**
	 * Checks if the control chain is actually controlling the Interaction.
	 * @param list the Control chain
	 * @param inter target Interaction
	 * @return true if the chain controls the Interaction
	 */
	private boolean search(LinkedList<Control> list, Interaction inter)
	{
		if (list.getLast().getControlled().contains(inter)) return true;

		for (Process process : list.getLast().getControlled())
		{
			if (process instanceof Control)
			{
				list.add((Control) process);
				if (search(list, inter)) return true;
				else list.removeLast();
			}
		}
		return false;
	}

	/**
	 * Gets input ot output participants of the Conversion.
	 * @param conv Conversion to get participants
	 * @param type input or output
	 * @return related participants
	 */
	protected Set<PhysicalEntity> getConvParticipants(Conversion conv, RelType type)
	{
		if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE)
		{
			HashSet<PhysicalEntity> set = new HashSet<PhysicalEntity>(conv.getLeft());
			set.addAll(conv.getRight());
			return set;
		}
		else if (conv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			return type == RelType.INPUT ? conv.getRight() : conv.getLeft();
		}
		else return type == RelType.OUTPUT ? conv.getRight() : conv.getLeft();
		
	}

	/**
	 * Asserts the size of teh parameter array is equal to the variable size.
	 * @param ind index array to assert its size
	 */
	protected void assertIndLength(int[] ind)
	{
		assert ind.length == getVariableSize();
	}
}
