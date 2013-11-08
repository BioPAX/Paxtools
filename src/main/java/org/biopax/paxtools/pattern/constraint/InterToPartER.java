package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This constraint is used to collect related EntityReference of the participant physical entities.
 * The constraint let's users to set some participants as taboo, and they are excluded from
 * analysis.
 *
 * Var0 - Interaction
 * Var1 - Taboo element number 1
 * ...
 * Var(numTabooElements+1) - Last taboo element
 * Var(numTabooElements+2) - The related EntityReference
 *
 * @author Ozgun Babur
 */
public class InterToPartER extends ConstraintAdapter
{
	/**
	 * Constraint used for traversing towards simpler PE.
	 */
	private static final LinkedPE linker = new LinkedPE(LinkedPE.Type.TO_MEMBER);

	/**
	 * Accessor from PE to ER.
	 */
	private static final PathAccessor pe2ER =
		new PathAccessor("SimplePhysicalEntity/entityReference");

	/**
	 * Constructor with parameters. A taboo element is the participant that we want to exclude from
	 * the analysis. User should provide the number of taboo elements, then during execution, these
	 * elements will be fetched from the current match.
	 */
	public InterToPartER(int numOfTabooElements)
	{
		super(numOfTabooElements + 2);
	}

	/**
	 * Constructor without parameters. There are no taboo elements.
	 */
	public InterToPartER()
	{
		super(2);
	}

	/**
	 * This is a generative constraint.
	 * @return true if the constraint can generate candidates
	 */
	@Override
	public boolean canGenerate()
	{
		return true;
	}

	/**
	 * Iterated over non-taboo participants and collectes related ER.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return related participants
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Interaction inter = (Interaction) match.get(ind[0]);

		Set<Entity> taboo = new HashSet<Entity>();

		for (int i = 1; i < getVariableSize() - 1; i++)
		{
			taboo.add((Entity) match.get(ind[i]));
		}

		Set<BioPAXElement> simples = new HashSet<BioPAXElement>();

		for (Entity part : inter.getParticipant())
		{
			if (part instanceof PhysicalEntity && !taboo.contains(part))
			{
				simples.addAll(linker.getLinkedElements((PhysicalEntity) part));
			}
		}

		return pe2ER.getValueFromBeans(simples);
	}
}
