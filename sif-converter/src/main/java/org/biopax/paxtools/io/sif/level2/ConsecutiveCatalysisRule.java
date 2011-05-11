package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.*;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.SEQUENTIAL_CATALYSIS;

/**
 * This class creates an interaction between two entities if they are catalyzing
 * consecutive conversions. Conversions are considered consecutive if one of the
 * RIGHT participants of one reaction is the LEFT of the other and if the
 * directions of catalysis and control matches. User: demir Date: Dec 28, 2007
 * Time: 10:40:01 PM
 */
public class ConsecutiveCatalysisRule implements InteractionRuleL2
{
	public void inferInteractions(Set<SimpleInteraction> interactionSet,
		Object entity,
		Model model, Map options)
	{
		inferInteractions(interactionSet, ((physicalEntity) entity), model, options);
	}

	public void inferInteractions(Set<SimpleInteraction> interactionSet,
		physicalEntity pe, Model model,
		Map options)
	{
		// Stop if options don't let to continue
		if (options.containsKey(SEQUENTIAL_CATALYSIS) &&
			options.get(SEQUENTIAL_CATALYSIS).equals(false))
		{
			return;
		}
		// OK, go on...
		Set<catalysis> catalysises = pe.getAllInteractions(catalysis.class);
		for (catalysis aCatalysis : catalysises)
		{
			processCatalysis(interactionSet, pe, aCatalysis);
		}
	}

	private void processCatalysis(
		Set<SimpleInteraction> interactionSet, physicalEntity pe, catalysis aCatalysis)
	{
		//We have to consider two direction statements
		//Catalysis.direction and Conversion.spontaneous
		//This method maps the former to the compatible latter
		//null means reversible or unknown, both are treated in the same way.
		SpontaneousType catalysisDirection =
			mapDirectionToSpontaneous(aCatalysis.getDIRECTION());
		//get the conversions and process them.
		Set<process> controlled = aCatalysis.getCONTROLLED();
		for (process process : controlled)
		{
			conversion aConversion = (conversion) process;
			//let's find the direction that is compatible with catalysis direction
			SpontaneousType direction =
				findConsensusDirection(catalysisDirection,
					aConversion.getSPONTANEOUS());
			assert direction != null;
			//and let's get the interacting physical entities
			createInteractions(aConversion, direction, pe, aCatalysis, interactionSet);
		}
	}

	/**
	 * This method will find the compatible conversion and catalysis direction
	 *
	 * @param direction1 type implied by catalysis
	 * @param direction2 type of the conversion
	 * @return
	 */
	private SpontaneousType findConsensusDirection(
		SpontaneousType direction1,
		SpontaneousType direction2)
	{
		SpontaneousType consensus;
		boolean first = isReversible(direction1);
		boolean second = isReversible(direction2);
		//	If any one of them is not-spontaneous than consensus is the other direction.
		//  If both of them are spontaneous, then consensus is null if they are spontenous
		//  in opposite directions.
		if (first)
		{
			if (second)
			{
				consensus = SpontaneousType.NOT_SPONTANEOUS;
			}
			else
			{
				consensus = direction2;
			}
		}
		else
		{
			if (second)
			{
				consensus = direction1;
			}
			else
			{
				consensus = direction1.equals(direction2) ? direction1 : null;
			}
		}
		return consensus;
	}

	private boolean isReversible(SpontaneousType direction1)
	{
		return direction1 == null ||
			direction1.equals(SpontaneousType.NOT_SPONTANEOUS);
	}

	private void createInteractions(conversion centerConversion,
		SpontaneousType direction,
		physicalEntity pe,
		catalysis aCatalysis,
		Set<SimpleInteraction> interactionSet)
	{
		//get the peps at the correct side of the conversion.
		Set<physicalEntityParticipant> peps =
			getCompatiblePEPs(direction, centerConversion);
		//for these set of peps find compatible conversions
		Set<conversion> conversions = getCompatibleConversions(peps, direction);

		// for each conversion find pes that catalyze them and add an interaction
		for (conversion neighbor : conversions)
		{
			findAndAddCatalysts(neighbor, direction, pe, aCatalysis, interactionSet);
		}
	}

	/**
	 * This method returns the PEPs that are on the correct side of the conversion
	 *
	 * @param direction   determining the side
	 * @param aConversion
	 * @return
	 */
	private Set<physicalEntityParticipant> getCompatiblePEPs(
		SpontaneousType direction,
		conversion aConversion)
	{
		switch (direction)
		{
			case L_R:
				return aConversion.getRIGHT();
			case R_L:
				return aConversion.getLEFT();
			default:
				return mergedSet(aConversion);
		}
	}

	private HashSet<physicalEntityParticipant> mergedSet(conversion aConversion)
	{
		HashSet<physicalEntityParticipant> hashSet =
			new HashSet<physicalEntityParticipant>();
		hashSet.addAll(aConversion.getLEFT());
		hashSet.addAll(aConversion.getRIGHT());
		return hashSet;
	}

	private Set<conversion> getCompatibleConversions(
		Set<physicalEntityParticipant> peps,
		SpontaneousType direction)
	{
		Set<conversion> compatibleConversions = new HashSet<conversion>();
		for (physicalEntityParticipant pep : peps)
		{
			Set<physicalEntityParticipant> npeps =
				pep.getPHYSICAL_ENTITY().isPHYSICAL_ENTITYof();
			for (physicalEntityParticipant npep : npeps)
			{
				if (!pep.equals(npep) && pep.isInEquivalentState(npep))
				{
					if (!npep.isPARTICIPANTSof().isEmpty())
					{
						assert npep.isPARTICIPANTSof().size() == 1;
						interaction anI = npep.isPARTICIPANTSof().iterator().next();

						if (anI instanceof conversion)
						{
							conversion aConversion = (conversion) anI;

							if (findConsensusDirection(direction,
								aConversion.getSPONTANEOUS()) != null &&
								participantIsAtACompatibleSide(direction,
									npep,
									aConversion))
							{
								compatibleConversions.add(aConversion);
							}
						}
					}
				}
			}
		}
		return compatibleConversions;
	}

	private boolean participantIsAtACompatibleSide(SpontaneousType direction,
		physicalEntityParticipant npep,
		conversion aConversion)
	{
		switch (direction)
		{
			case L_R:
				return aConversion.getLEFT().contains(npep);
			case R_L:
				return aConversion.getRIGHT().contains(npep);
			default:
				return aConversion.getRIGHT().contains(npep) ||
					aConversion.getLEFT().contains(npep);
		}
	}

	private void findAndAddCatalysts(conversion aConversion,
		SpontaneousType direction, physicalEntity pe, catalysis aCatalysis, Set<SimpleInteraction> interactionSet)
	{
		Set<control> controls =
			aConversion.isCONTROLLEDOf();
		for (catalysis consequentCatalysis : new ClassFilterSet<control,catalysis>(
			controls, catalysis.class))
		{
			if (findConsensusDirection(direction,
				mapDirectionToSpontaneous(consequentCatalysis.getDIRECTION())) != null)
			{
				for (physicalEntityParticipant pepi : consequentCatalysis.getCONTROLLER())
				{
					//create interactions and add to set
					SimpleInteraction si =
						new SimpleInteraction(pe,
							pepi.getPHYSICAL_ENTITY(),
							SEQUENTIAL_CATALYSIS);
					si.addMediator(aCatalysis);
					si.addMediator(consequentCatalysis);
					interactionSet.add(si);
				}
			}
		}
	}

	private SpontaneousType mapDirectionToSpontaneous(Direction direction)
	{
		if (direction != null)
		{
			switch (direction)
			{
				case IRREVERSIBLE_LEFT_TO_RIGHT:
				case PHYSIOL_LEFT_TO_RIGHT:
					return SpontaneousType.L_R;
				case IRREVERSIBLE_RIGHT_TO_LEFT:
				case PHYSIOL_RIGHT_TO_LEFT:
					return SpontaneousType.R_L;
			}
		}
		return null;
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(SEQUENTIAL_CATALYSIS);
	}
}
