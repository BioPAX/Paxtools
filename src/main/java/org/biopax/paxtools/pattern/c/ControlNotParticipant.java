package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.Match;

import java.util.Set;

/**
 * Checks if a controller of this Control is also a participant of the controlled interactions. It
 * satisfies if not.
 *
 * var0 is a Control
 *
 * @author Ozgun Babur
 */
public class ControlNotParticipant extends ConstraintAdapter
{
	/**
	 * This is a single element constraint
	 * @return 1
	 */
	@Override
	public int getVariableSize()
	{
		return 1;
	}

	/**
	 * Checks if the controlled Interaction contains a controller as a participant. This constraint
	 * filters out such cases.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if participants of teh controlled Interactions not also a controller of the
	 * Control.
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		Control ctrl = (Control) match.get(ind[0]);

		for (Process process : ctrl.getControlled())
		{
			if (process instanceof Interaction)
			{
				Interaction inter = (Interaction) process;
				Set<Entity> participant = inter.getParticipant();
				for (Controller controller : ctrl.getController())
				{
					if (participant.contains(controller)) return false;
				}
			}
		}
		return true;
	}
}
