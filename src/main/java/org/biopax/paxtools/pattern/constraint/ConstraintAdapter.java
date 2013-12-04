package org.biopax.paxtools.pattern.constraint;

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
	 * Size of the constraint. This is defined by how many elements this constraint needs to be \
	 * mapped to work.
	 */
	protected int size;

	/**
	 * Constructor with size.
	 * @param size size if the constraint.
	 */
	protected ConstraintAdapter(int size)
	{
		this.size = size;
	}

	/**
	 * Empty constructor. Note that specifying the size is not mandatory since the child constraint
	 * can override <code>getVariableSize</code> instead of using the size variable.
	 */
	protected ConstraintAdapter()
	{
	}

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
	 * Asserts the size of teh parameter array is equal to the variable size.
	 * @param ind index array to assert its size
	 */
	protected void assertIndLength(int[] ind)
	{
		assert ind.length == getVariableSize();
	}

	/**
	 * Sets the size of the constraint.
	 * @param size size of the constraint
	 */
	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * Gets the variable size of the constraint.
	 * @return variable size
	 */
	@Override
	public int getVariableSize()
	{
		return size;
	}

	//----- Section: Common BioPAX Operations -----------------------------------------------------|

	/**
	 * Gets the direction of the Control chain the the Interaction.
	 * @param conv controlled conversion
	 * @param cont top control
	 * @return the direction of the conversion related to the catalysis
	 */
	protected ConversionDirectionType getDirection(Conversion conv, Control cont)
	{
		// If conversion direction is specified and it is one way, then control direction does not
		// have any effect
		if (conv.getConversionDirection() != null &&
				conv.getConversionDirection() != ConversionDirectionType.REVERSIBLE)
			return conv.getConversionDirection();

		// else if the control chain have a direction, use it
		for (Control ctrl : getControlChain(cont, conv))
		{
			ConversionDirectionType dir = getCatalysisDirection(ctrl);
			if (dir != null) return dir;
		}

		// else use any direction found
		return getDirection(conv);
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
	 * Otherwise a RuntimeException is thrown. This assumes that there is only one control chain
	 * towards the interaction. It not, then one of the chains will be returned.
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
				// prevent searching in cycles
				if (list.contains(process)) continue;

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
		ConversionDirectionType dir = getDirection(conv);
		if (dir == ConversionDirectionType.REVERSIBLE)
		{
			HashSet<PhysicalEntity> set = new HashSet<PhysicalEntity>(conv.getLeft());
			set.addAll(conv.getRight());
			return set;
		}
		else if (dir == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			return type == RelType.INPUT ? conv.getRight() : conv.getLeft();
		}
		else return type == RelType.OUTPUT ? conv.getRight() : conv.getLeft();
	}

	/**
	 * Searches pathways that contains this conversion for the possible directions. If both
	 * directions exist, then the result is reversible.
	 * @param conv the conversion
	 * @return direction inferred from pathway membership
	 */
	protected ConversionDirectionType findDirectionInPathways(Conversion conv)
	{
		Set<StepDirection> dirs = new HashSet<StepDirection>();
		for (PathwayStep step : conv.getStepProcessOf())
		{
			if (step instanceof BiochemicalPathwayStep)
			{
				StepDirection dir = ((BiochemicalPathwayStep) step).getStepDirection();
				if (dir != null) dirs.add(dir);
			}
		}
		if (dirs.size() > 1) return ConversionDirectionType.REVERSIBLE;
		else if (!dirs.isEmpty())
		{
			return dirs.iterator().next() == StepDirection.LEFT_TO_RIGHT ?
				ConversionDirectionType.LEFT_TO_RIGHT : ConversionDirectionType.RIGHT_TO_LEFT;
		}
		else return null;
	}

	/**
	 * Searches the controlling catalysis for possible direction of the conversion.
	 * @param conv the conversion
	 * @return direction inferred from catalysis objects
	 */
	protected ConversionDirectionType findDirectionInCatalysis(Conversion conv)
	{
		Set<ConversionDirectionType> dirs = new HashSet<ConversionDirectionType>();
		for (Control control : conv.getControlledOf())
		{
			ConversionDirectionType dir = getCatalysisDirection(control);
			if (dir != null) dirs.add(dir);
		}
		if (dirs.size() > 1) return ConversionDirectionType.REVERSIBLE;
		else if (!dirs.isEmpty()) return dirs.iterator().next();
		else return null;
	}

	protected ConversionDirectionType getDirection(Conversion conv)
	{
		if (conv.getConversionDirection() != null) return conv.getConversionDirection();

		ConversionDirectionType catDir = findDirectionInCatalysis(conv);
		ConversionDirectionType patDir = findDirectionInPathways(conv);

		if (catDir != null && patDir != null && catDir != patDir)
			return ConversionDirectionType.REVERSIBLE;
		else if (catDir != null) return catDir;
		else return patDir;
	}
}
