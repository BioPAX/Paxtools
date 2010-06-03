package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class AffectsRule implements InteractionRuleL2
{
	public void inferInteractions(Set<SimpleInteraction> interactionSet,
								  physicalEntity A,
								  Model model,
								  Map options)
	{
		for (control cont : A.getAllInteractions(control.class))
		{
			for (process prcs : cont.getCONTROLLED())
			{
				if (prcs instanceof conversion)
				{
					createInteractions(interactionSet, A, (conversion) prcs, cont);
				}
			}
		}
	}

	public void inferInteractions(Set<SimpleInteraction> interactionSet,
								  Object entity,
								  Model model,
								  Map options)
	{
		inferInteractions(interactionSet, (physicalEntity) entity, model, options );
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(
			BinaryInteractionType.ACTIVATES,
			BinaryInteractionType.INACTIVATES);
	}

	private boolean isActive(physicalEntityParticipant pep)
	{
		if (affectsSomething(pep) && !isAnnotatedInactive(pep) &&
			(!(pep.getPHYSICAL_ENTITY() instanceof complex) ||
				!complexHasInactiveMember((complex) pep.getPHYSICAL_ENTITY())))
		{
			return true;
		}

		if (isAnnotatedActive(pep))
		{
			return true;
		}

		if (pep.getPHYSICAL_ENTITY() instanceof complex &&
			complexHasActiveMember((complex) pep.getPHYSICAL_ENTITY()) &&
			!complexHasInactiveMember((complex) pep.getPHYSICAL_ENTITY()))
		{
			return true;
		}

		return false;
	}

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

//		String name = pep.getPHYSICAL_ENTITY().getNAME().toLowerCase();
//
//		if (name.contains("activ") && !name.contains("inactiv"))
//		{
//			return true;
//		}

		return false;
	}

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

//		if (pep.getPHYSICAL_ENTITY().getNAME() != null)
//		{
//			String name = pep.getPHYSICAL_ENTITY().getNAME().toLowerCase();
//
//			if (name.contains("inactiv"))
//			{
//				return true;
//			}
//		}

		return false;
	}

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

	private boolean complexHasActiveMember(complex cmp)
	{
		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (isAnnotatedActive(pep))
			{
				return true;
			}
			else if (pep.getPHYSICAL_ENTITY() instanceof complex &&
				complexHasActiveMember((complex) pep.getPHYSICAL_ENTITY()))
			{
				return true;
			}
		}
		return false;
	}

	private boolean complexHasInactiveMember(complex cmp)
	{
		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (isAnnotatedInactive(pep))
			{
				return true;
			}
			else if (pep.getPHYSICAL_ENTITY() instanceof complex &&
				complexHasInactiveMember((complex) pep.getPHYSICAL_ENTITY()))
			{
				return true;
			}
		}
		return false;
	}

	private Set<physicalEntity> getMembers(complex cmp, boolean active, Set<physicalEntity> set)
	{
		if (set == null) set = new HashSet<physicalEntity>();

		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (!(pep.getPHYSICAL_ENTITY() instanceof complex) &&
				((active && isAnnotatedActive(pep)) ||
					(!active && isAnnotatedInactive(pep))))
			{
				set.add(pep.getPHYSICAL_ENTITY());
			}
			else if (pep.getPHYSICAL_ENTITY() instanceof complex)
			{
				getMembers((complex) pep.getPHYSICAL_ENTITY(), active, set);
			}
		}
		return set;
	}

	private Set<physicalEntityParticipant> getMembers(complex cmp, Set<physicalEntityParticipant> set)
	{
		if (set == null) set = new HashSet<physicalEntityParticipant>();

		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (pep.getPHYSICAL_ENTITY() instanceof complex)
			{
				getMembers((complex) pep.getPHYSICAL_ENTITY(), set);
			}
			else
			{
				set.add(pep);
			}
		}
		return set;
	}

	private void createInteractions (
		Set<SimpleInteraction> interactionSet,
		physicalEntity A,
		conversion conv,
		control cont)
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
					interactionSet.add(new SimpleInteraction(A, tuple[1].getPHYSICAL_ENTITY(),
						BinaryInteractionType.ACTIVATES));
				}
				else
				{
					assert sign == -1;

					interactionSet.add(new SimpleInteraction(A, tuple[1].getPHYSICAL_ENTITY(),
						BinaryInteractionType.INACTIVATES));
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
					interactionSet.add(new SimpleInteraction(A, pep.getPHYSICAL_ENTITY(),
						BinaryInteractionType.ACTIVATES));
				}
				else if (sign == -1)
				{
					interactionSet.add(new SimpleInteraction(A, pep.getPHYSICAL_ENTITY(),
						BinaryInteractionType.INACTIVATES));
				}

				if (sign != 0)
				{
					for (physicalEntity pe :
						eff == 1 ?
							getMembers((complex) pep.getPHYSICAL_ENTITY(), true, null) :
							getMembers((complex) pep.getPHYSICAL_ENTITY(), false, null))
					{
						interactionSet.add(new SimpleInteraction(pe, pep.getPHYSICAL_ENTITY(),
							BinaryInteractionType.ACTIVATES));
					}
				}
			}
		}
	}

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

	private Set<physicalEntityParticipant[]> getEntityMatching(
		Set<physicalEntityParticipant> set1,
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
