package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * var0 is a Control
 *
 * Checks if a controller of this Control is also a participant of the controlled interactions. It
 * satisfies if not.
 *
 * @author Ozgun Babur
 */
public class ControlNotParticipant extends ConstraintAdapter
{
	@Override
	public int getVariableSize()
	{
		return 1;
	}

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
