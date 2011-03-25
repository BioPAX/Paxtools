package org.biopax.paxtools.util;

import org.biopax.paxtools.model.level2.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class has static methods for doing some calculations on biopax level 2 model members. These
 * methods may be considered to be included in the utility methods of the model objects themselves.
 * However, most of these methods are based on very weak assumptions, and most times they work
 * because of the common missuse of biopax by data providers.
 *
 * @author Ozgun Babur Date: Mar 27, 2008 Time: 4:33:57 PM
 */
public class Level2Helper
{
	public static boolean hasLeftToRightEvidence(conversion conv, control cont)
	{

		if (conv != null && cont != null)
		{
			if (cont instanceof catalysis)
			{
				Direction dir = ((catalysis) cont).getDIRECTION();

				if (dir != null)
				{
					if (dir == Direction.REVERSIBLE ||
					    dir == Direction.IRREVERSIBLE_LEFT_TO_RIGHT ||
					    dir == Direction.PHYSIOL_LEFT_TO_RIGHT)

					{
						return true;
					}

				}
			}
			if (cont.getCONTROLLED().contains(conv))
			{
				// Try to understand from spontaneous field of conversion
				return conv.getSPONTANEOUS() == SpontaneousType.L_R;
			}
		}
		return false;

	}

	public static boolean hasRightToLeftEvidence(conversion conv, control cont)
	{
		if (cont != null)
		{
			if (cont instanceof catalysis)
			{
				Direction dir = ((catalysis) cont).getDIRECTION();

				if (dir != null)
				{
					if (dir == Direction.REVERSIBLE)
					{
						return true;
					}

					if (dir == Direction.IRREVERSIBLE_RIGHT_TO_LEFT ||
					    dir == Direction.PHYSIOL_RIGHT_TO_LEFT)
					{
						return true;
					}
				}
			}

			if (cont.getCONTROLLED().contains(conv))
			{
				// Try to understand from spontaneous field of conversion
				return conv.getSPONTANEOUS() == SpontaneousType.R_L;
			}
		}
		return false;
	}

	public static boolean isNegative
			(control
					cont)
	{
		boolean negative = false;

		ControlType type = cont.getCONTROL_TYPE();
		if (type != null)
		{
			if (type == ControlType.INHIBITION ||
			    type == ControlType.INHIBITION_ALLOSTERIC ||
			    type == ControlType.INHIBITION_COMPETITIVE ||
			    type == ControlType.INHIBITION_IRREVERSIBLE ||
			    type == ControlType.INHIBITION_NONCOMPETITIVE ||
			    type == ControlType.INHIBITION_OTHER ||
			    type == ControlType.INHIBITION_UNCOMPETITIVE ||
			    type == ControlType.INHIBITION_UNKMECH)
			{
				negative = true;
			}
		}
		return negative;
	}

	public static Collection<List<physicalEntityParticipant>> groupParticipants
			(physicalEntity
					pe,
			 boolean includeComplexMemberships)
	{
		Collection<List<physicalEntityParticipant>> set =
				new ArrayList<List<physicalEntityParticipant>>();

		for (physicalEntityParticipant par : pe.isPHYSICAL_ENTITYof())
		{
			if (!includeComplexMemberships && par.isCOMPONENTof() != null)
			{
				continue;
			}

			List<physicalEntityParticipant> parts = null;

			for (List<physicalEntityParticipant> e : set)
			{
				if (e.get(0).isInEquivalentState(par))
				{
					parts = e;
				}
			}

			if (isUbique(pe) || parts == null)
			{
				parts = new ArrayList<physicalEntityParticipant>();
				set.add(parts);
			}
			parts.add(par);
		}

		return set;
	}

	public static Collection<List<physicalEntityParticipant>> selectFunctionalSets
			(
					Collection<List<physicalEntityParticipant>> parts)
	{
		Collection<List<physicalEntityParticipant>> functional =
				new ArrayList<List<physicalEntityParticipant>>();

		for (List<physicalEntityParticipant> list : parts)
		{
			for (physicalEntityParticipant pep : list)
			{
				if (functional.contains(list))
				{
					break;
				}

				// Check if this participant points to a control

				for (interaction inter : pep.isPARTICIPANTSof())
				{
					if (inter instanceof control)
					{
						if (!functional.contains(list))
						{
							functional.add(list);
							break;
						}
					}
				}

				if (!functional.contains(list))
				{
					// Check if the participant labeled as active

					if (pep instanceof sequenceParticipant)
					{
						if (Level2Helper.hasActiveLabel((sequenceParticipant) pep))
						{
							functional.add(list);
							break;
						}
					}
				}

				if (!functional.contains(list))
				{
					// Check if the parent complex has an activity

					complex cmp = pep.isCOMPONENTof();

					if (cmp != null)
					{
						if (!cmp.getAllInteractions(control.class).isEmpty())
						{
							functional.add(list);
							break;
						}
					}
				}
			}
		}
		return functional;
	}

	public static Collection<List<physicalEntityParticipant>> selectInactiveSets
			(
					Collection<List<physicalEntityParticipant>> parts)
	{
		Collection<List<physicalEntityParticipant>> inactive =
				new ArrayList<List<physicalEntityParticipant>>();

//		physicalEntity pe = null;

		for (List<physicalEntityParticipant> list : parts)
		{
			for (physicalEntityParticipant pep : list)
			{
				// Check if participant has an inactive label

				if (pep instanceof sequenceParticipant)
				{
					if (Level2Helper.hasInactiveLabel((sequenceParticipant) pep))
					{
						inactive.add(list);
						break;
					}
				}
//				else
//				{
//					pe = pep.getPHYSICAL_ENTITY();
//				}
			}
		}

		// If no inactive set is found, then check if this entity is a complex and contains any
		// inactive member. If found, then all sets points to an inactive complex.

//		if (pe != null && pe instanceof complex)
//		{
//			if (isInactiveComplex((complex) pe))
//			{
//				inactive.addAll(parts);
//			}
//		}

		return inactive;
	}

	/**
	 * Checks if any recursive members of complex has the OCV term specified.
	 *
	 * @param cmp  complex to check
	 * @param term prefix of the term to search
	 * @return true if any member (recursive) contains a term starts with the parameter
	 */
	private static boolean complexHasLabeledMember
			(complex
					cmp, String
					term)
	{
		for (physicalEntityParticipant pep : cmp.getCOMPONENTS())
		{
			if (pep instanceof sequenceParticipant)
			{
				if (hasSeqFeatOCVTerm((sequenceParticipant) pep, term))
				{
					return true;
				}
			}
			else if (pep.getPHYSICAL_ENTITY() instanceof complex)
			{
				if (complexHasLabeledMember((complex) pep.getPHYSICAL_ENTITY(), term))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isActiveComplex
			(complex
					cmp)
	{
		for (physicalEntityParticipant pep : cmp.isPHYSICAL_ENTITYof())
		{
			for (interaction inter : pep.isPARTICIPANTSof())
			{
				if (inter instanceof control)
				{
					return true;
				}
			}
		}
		return complexHasLabeledMember(cmp, "active");
	}

	public static boolean isInactiveComplex
			(complex
					cmp)
	{
		for (physicalEntityParticipant pep : cmp.isPHYSICAL_ENTITYof())
		{
			for (interaction inter : pep.isPARTICIPANTSof())
			{
				if (inter instanceof control)
				{
					return false;
				}
			}
		}
		return complexHasLabeledMember(cmp, "inactive");
	}

	private static boolean isUbique
			(physicalEntity
					pe)
	{
		return pe instanceof smallMolecule &&
		       (pe.getNAME().startsWith("ATP") ||
		        pe.getNAME().startsWith("ADP") ||
		        pe.getNAME().startsWith("GTP"));
	}

	private static boolean hasActiveLabel
			(sequenceParticipant
					sp)
	{
		return hasSeqFeatOCVTerm(sp, "active");
	}

	private static boolean hasInactiveLabel
			(sequenceParticipant
					sp)
	{
		return hasSeqFeatOCVTerm(sp, "inactive");
	}

	private static boolean hasSeqFeatOCVTerm
			(sequenceParticipant
					sp, String
					term)
	{
		for (sequenceFeature sf : sp.getSEQUENCE_FEATURE_LIST())
		{
			openControlledVocabulary ocv = sf.getFEATURE_TYPE();
			if (ocv != null)
			{
				Set<String> terms = ocv.getTERM();

				if (terms != null)
				{
					for (String t : terms)
					{
						if (t.startsWith(term))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
