package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.util.*;

/**
 * This is an experimental rule that mines ACTIVATES and INHIBITS relations between molecules.
 * @author Ozgun Babur
 */
public class AffectsRule extends InteractionRuleL2Adaptor
{
	/**
	 * Supported interaction types.
	 */
	private static List<BinaryInteractionType> binaryInteractionTypes =
		Arrays.asList(BinaryInteractionType.ACTIVATES, BinaryInteractionType .INACTIVATES);

	/**
	 * Infers interactions using the given physicalEntity as seed.
	 * @param interactionSet to be populated
	 * @param pe first physicalEntity
	 * @param model BioPAX model
	 */
	@Override public void inferInteractionsFromPE(InteractionSet interactionSet, physicalEntity pe,
		Model model)
	{
		for (control cont : pe.getAllInteractions(control.class))
		{
			for (process prcs : cont.getCONTROLLED())
			{
				if (prcs instanceof conversion)
				{
					createInteractions(interactionSet, pe, (conversion) prcs, cont);
				}
			}
		}
	}

	/**
	 * Gets supported interactions types.
	 * @return supported interactions types
	 */
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}

	/**
	 * Checks of the participant has an activity.
	 * @param pep participant to check
	 * @return true if there is evidence for activity
	 */
	private boolean isActive(physicalEntityParticipant pep)
	{
		if (affectsSomething(pep) && !isAnnotatedInactive(pep) &&
		    (!(pep.getPHYSICAL_ENTITY() instanceof complex) || !complexHasInactiveMember(
				    (complex) pep.getPHYSICAL_ENTITY())))
		{
			return true;
		}

		if (isAnnotatedActive(pep))
		{
			return true;
		}

		if (pep.getPHYSICAL_ENTITY() instanceof
			complex && complexHasActiveMember((complex) pep.getPHYSICAL_ENTITY()) &&
		    !complexHasInactiveMember((complex) pep.getPHYSICAL_ENTITY()))
		{
			return true;
		}

		return false;
	}

	/**
	 * Checks of the participant has evidence for inactivity.
	 * @param pep participant to check
	 * @return true if there is evidence for inactivity
	 */
	private boolean isInactive(physicalEntityParticipant pep)
	{
		if (isAnnotatedInactive(pep))
		{
			return true;
		}

		if (pep.getPHYSICAL_ENTITY() instanceof complex &&
			complexHasInactiveMember((complex) pep.getPHYSICAL_ENTITY()) &&
			!complexHasActiveMember((complex) pep.getPHYSICAL_ENTITY()))
		{
			return true;
		}

		return false;
	}

	/**
	 * Checks if there is an annotation for the participant for its activity.
	 * @param pep participant to check
	 * @return true if there is an annotation for activity
	 */
	private boolean isAnnotatedActive(physicalEntityParticipant pep)
	{
		if (pep instanceof sequenceParticipant)
		{
			for (sequenceFeature sf : ((sequenceParticipant) pep).getSEQUENCE_FEATURE_LIST())
			{
				if (sf.getFEATURE_TYPE() != null)
				{
					for (String term : sf.getFEATURE_TYPE().getTERM())
					{
						if (term.startsWith("active"))
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks if there is an annotation for the participant for its inactivity.
	 * @param pep participant to check
	 * @return true if there is an annotation for inactivity
	 */
	private boolean isAnnotatedInactive(physicalEntityParticipant pep)
	{
		if (pep instanceof sequenceParticipant)
		{
			for (sequenceFeature sf : ((sequenceParticipant) pep).getSEQUENCE_FEATURE_LIST())
			{
				if (sf.getFEATURE_TYPE() != null)
				{
					for (String term : sf.getFEATURE_TYPE().getTERM())
					{
						if (term.startsWith("inactive") || term.startsWith("ubiquitin"))
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks if this participant or the the ones in equivalent state are effecting something.
	 * @param pep participant to check
	 * @return true if this one or an equivalent affects something
	 */
	private boolean affectsSomething(physicalEntityParticipant pep)
	{
		physicalEntity pe = pep.getPHYSICAL_ENTITY();

		for (physicalEntityParticipant par : pe.isPHYSICAL_ENTITYof())
		{
			if (par != pep && par.isInEquivalentState(pep))
			{
				for (interaction inter : par.isPARTICIPANTSof())
				{
					if (inter instanceof control)
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the complex has an active member.
	 * @param cmp complex to check
	 * @return true if complex has an active member
	 */
	private boolean complexHasActiveMember(complex cmp)
	{
		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (isAnnotatedActive(pep))
			{
				return true;
			} else if (pep.getPHYSICAL_ENTITY() instanceof complex && complexHasActiveMember(
					(complex) pep.getPHYSICAL_ENTITY()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the complex has an inactive member.
	 * @param cmp complex to check
	 * @return true if complex has an inactive member
	 */
	private boolean complexHasInactiveMember(complex cmp)
	{
		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (isAnnotatedInactive(pep))
			{
				return true;
			} else if (pep.getPHYSICAL_ENTITY() instanceof complex && complexHasInactiveMember(
					(complex) pep.getPHYSICAL_ENTITY()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets active or inactive members of a complex.
	 * @param cmp complex to get members
	 * @param active desired activity state
	 * @param set set to collect to, initialized if null
	 * @return same set
	 */
	private Set<physicalEntity> getMembers(complex cmp, boolean active, Set<physicalEntity> set)
	{
		if (set == null) set = new HashSet<physicalEntity>();

		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (!(pep.getPHYSICAL_ENTITY() instanceof complex) &&
				((active && isAnnotatedActive(pep)) || (!active && isAnnotatedInactive(pep))))
			{
				set.add(pep.getPHYSICAL_ENTITY());
			} else if (pep.getPHYSICAL_ENTITY() instanceof complex)
			{
				getMembers((complex) pep.getPHYSICAL_ENTITY(), active, set);
			}
		}
		return set;
	}

	/**
	 * Gets members of the complex recursively.
	 * @param cmp complex to get members
	 * @param set set to collect to
	 * @return members
	 */
	private Set<physicalEntityParticipant> getMembers(complex cmp,
		Set<physicalEntityParticipant> set)
	{
		if (set == null) set = new HashSet<physicalEntityParticipant>();

		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (pep.getPHYSICAL_ENTITY() instanceof complex)
			{
				getMembers((complex) pep.getPHYSICAL_ENTITY(), set);
			} else
			{
				set.add(pep);
			}
		}
		return set;
	}

	/**
	 * Creates the inferred interaction.
	 * @param interactionSet set to add new interactions
	 * @param A seed of the interaction
	 * @param conv conversion that affects B
	 * @param cont control where A is controller
	 */
	private void createInteractions(InteractionSet  interactionSet, physicalEntity A,
		conversion conv, control cont)
	{
		boolean l2r = true;

		if (cont instanceof catalysis)
		{
			Direction dir = ((catalysis) cont).getDIRECTION();

			if (dir != null)
			{
				if (dir == Direction.REVERSIBLE) return;

				if (dir == Direction.IRREVERSIBLE_RIGHT_TO_LEFT ||
					dir == Direction.PHYSIOL_RIGHT_TO_LEFT)
				{
					l2r = false;
				}
			}
		}

		int effect = 1;
		if (cont != null && cont.getCONTROL_TYPE() != null)
		{
			effect = cont.getCONTROL_TYPE().toString().startsWith("ACT") ? 1 : -1;
		}

		Set<physicalEntityParticipant> input = l2r ? conv.getLEFT() : conv.getRIGHT();
		Set<physicalEntityParticipant> output = !l2r ? conv.getLEFT() : conv.getRIGHT();

		enrichWithMembers(input);
		enrichWithMembers(output);

		Set<physicalEntityParticipant[]> matching = getEntityMatching(input, output);

		for (physicalEntityParticipant[] tuple : matching)
		{
			int inp = isActive(tuple[0]) ? 1 : isInactive(tuple[0]) ? -1 : 0;
			int oup = isActive(tuple[1]) ? 1 : isInactive(tuple[1]) ? -1 : 0;

			if (oup != 0 && inp != oup)
			{
				int sign = oup * effect;

				assert sign != 0;

				if (sign == 1)
				{
					interactionSet.add(new SimpleInteraction(
						A, tuple[1].getPHYSICAL_ENTITY(), BinaryInteractionType.ACTIVATES));
				} else
				{
					assert sign == -1;

					interactionSet.add(new SimpleInteraction(
						A, tuple[1].getPHYSICAL_ENTITY(), BinaryInteractionType.INACTIVATES));
				}
			}
		}

		for (physicalEntityParticipant pep : output)
		{
			if (pep.getPHYSICAL_ENTITY() instanceof complex)
			{
				int eff = isActive(pep) ? 1 : isInactive(pep) ? -1 : 0;
				int sign = eff * effect;

				if (sign == 1)
				{
					interactionSet.add(new SimpleInteraction(
						A, pep.getPHYSICAL_ENTITY(), BinaryInteractionType.ACTIVATES));
				}
				else if (sign == -1)
				{
					interactionSet.add(new SimpleInteraction(
						A, pep.getPHYSICAL_ENTITY(), BinaryInteractionType.INACTIVATES));
				}

				if (sign != 0)
				{
					for (physicalEntity pe :
						eff == 1 ? getMembers((complex) pep.getPHYSICAL_ENTITY(), true, null) :
							getMembers((complex) pep.getPHYSICAL_ENTITY(), false, null))
					{
						interactionSet.add(new SimpleInteraction(
							pe, pep.getPHYSICAL_ENTITY(), BinaryInteractionType.ACTIVATES));
					}
				}
			}
		}
	}

	/**
	 * Enriches the given set with members of the complexes in the set.
	 * @param set set to enrich
	 */
	private void enrichWithMembers(Set<physicalEntityParticipant> set)
	{
		for (physicalEntityParticipant pep : new HashSet<physicalEntityParticipant>(set))
		{
			if (pep.getPHYSICAL_ENTITY() instanceof complex)
			{
				set.addAll(getMembers((complex) pep.getPHYSICAL_ENTITY(), null));
			}
		}
	}

	/**
	 * Gets the pairs of participants in two sets, where their physicalEntity is the same.
	 * @param set1 first set
	 * @param set2 second set
	 * @return pairs of participants
	 */
	private Set<physicalEntityParticipant[]> getEntityMatching(Set<physicalEntityParticipant> set1,
		Set<physicalEntityParticipant> set2)
	{
		Set<physicalEntityParticipant[]> tuples = new HashSet<physicalEntityParticipant[]>();

		for (physicalEntityParticipant pep1 : set1)
		{
			for (physicalEntityParticipant pep2 : set2)
			{
				if (pep1.getPHYSICAL_ENTITY() == pep2.getPHYSICAL_ENTITY())
				{
					tuples.add(new physicalEntityParticipant[]{pep1, pep2});
				}
			}
		}
		return tuples;
	}
}
