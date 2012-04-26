package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public abstract class ConstraintAdapter implements Constraint
{
	/**
	 * If you override this method, then don't forget to also override getGeneratedInd, and generate
	 * methods.
	 */
	@Override
	public boolean canGenerate()
	{
		return false;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int ... ind)
	{
		throw new RuntimeException("This constraint is not generative. " +
			"Please check with canGenerate first.");
	}

	/**
	 * Use this method only if constraint canGenerate, and satisfaction criteria is that simple.
	 *
	 * @param match
	 * @param ind
	 * @return
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return generate(match, ind).contains(match.get(ind[ind.length - 1]));
	}

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
	 * Use this method only if you are sure that there is a link from the control to conversion.
	 * Otherwise a RuntimeException is thrown.
	 *
	 * @param control
	 * @param conv
	 * @return
	 */
	protected List<Control> getControlChain(Control control, Conversion conv)
	{
		LinkedList<Control> list = new LinkedList<Control>();
		list.add(control);

		boolean found = search(list, conv);

		if (!found) throw new RuntimeException("No link from Control to Conversion.");

		return list;
	}

	private boolean search(LinkedList<Control> list, Conversion conv)
	{
		if (list.getLast().getControlled().contains(conv)) return true;

		for (Process process : list.getLast().getControlled())
		{
			if (process instanceof Control)
			{
				list.add((Control) process);
				if (search(list, conv)) return true;
				else list.removeLast();
			}
		}
		return false;
	}
	
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
}
