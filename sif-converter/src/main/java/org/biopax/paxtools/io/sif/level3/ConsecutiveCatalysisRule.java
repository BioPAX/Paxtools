package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.SEQUENTIAL_CATALYSIS;

/**
 * This class creates an interaction between two entities if they are catalyzing consecutive
 * conversions. Conversions are considered consecutive if one of the RIGHT participants of one
 * reaction is the LEFT of the other and if the directions of catalysis and control matches.
 * @author Emek Demir
 */
public class ConsecutiveCatalysisRule extends InteractionRuleL3Adaptor
{
	private static List<BinaryInteractionType> binaryInteractionTypes = Arrays.asList(SEQUENTIAL_CATALYSIS);


	public void inferInteractionsFromPE(InteractionSetL3 interactionSet, PhysicalEntity pe, Model model)
	{

		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Catalysis)
			{
				BioPAXElement bpe = this.getEntityReferenceOrGroup(pe, interactionSet);
				processCatalysis(interactionSet, (Catalysis) inter, bpe);
			}
		}
	}

	private void processCatalysis(InteractionSetL3 interactionSet, Catalysis aCatalysis, BioPAXElement bpe)
	{
		//We have to consider two direction statements
		//Catalysis.direction and Conversion.spontaneous
		//This method maps the former to the compatible latter
		//null means reversible or unknown, both are treated in the same way.
		ConversionDirectionType catalysisDirection = mapDirectionToConversion(aCatalysis.getCatalysisDirection());

		//get the conversions and process them.
		Set<Process> controlled = aCatalysis.getControlled();
		for (Process process : controlled)
		{
			assert process instanceof Conversion;

			Conversion aConversion = (Conversion) process;

			//let's find the direction that is compatible with catalysis direction
			ConversionDirectionType dirA =
					findConsensusDirection(catalysisDirection, aConversion.getConversionDirection());

			assert dirA != null;

			//and let's get the interacting physical entities
			createInteractions(aConversion, dirA, aCatalysis, interactionSet, bpe);
		}
	}

	/**
	 * This method will find the compatible conversion and catalysis direction
	 * @param direction1 type implied by catalysis
	 * @param direction2 type of the conversion
	 * @return
	 */
	private ConversionDirectionType findConsensusDirection(ConversionDirectionType direction1,
			ConversionDirectionType direction2)
	{
		ConversionDirectionType consensus;
		boolean first = isReversible(direction1);
		boolean second = isReversible(direction2);

		//	If any one of them is not-spontaneous than consensus is the other direction.
		//  If both of them are spontaneous, then consensus is null if they are spontenous
		//  in opposite directions.
		if (first)
		{
			if (second)
			{
				consensus = ConversionDirectionType.REVERSIBLE;
			} else
			{
				consensus = direction2;
			}
		} else
		{
			if (second)
			{
				consensus = direction1;
			} else
			{
				consensus = direction1.equals(direction2) ? direction1 : null;
			}
		}
		return consensus;
	}

	private boolean isReversible(ConversionDirectionType direction1)
	{
		return direction1 == null || direction1.equals(ConversionDirectionType.REVERSIBLE);
	}

	private void createInteractions(Conversion centerConversion, ConversionDirectionType dirA, Catalysis aCatalysis,
			InteractionSetL3 interactionSet, BioPAXElement bpe)
	{
		//get the pes at the correct side of the conversion.
		Set<PhysicalEntity> pes = getOutputPEs(dirA, centerConversion);

		for (PhysicalEntity pe : pes)
		{
			for (Interaction inter : pe.getParticipantOf())
			{
				if (inter instanceof Conversion)
				{
					Conversion nextConversion = (Conversion) inter;

					if (centerConversion == nextConversion)
					{
						continue;
					}

					Set<Control> controls = nextConversion.getControlledOf();

					for (Catalysis consequentCatalysis : new ClassFilterSet<Control, Catalysis>(controls,
					                                                                            Catalysis.class))
					{
						ConversionDirectionType direction2 =
								mapDirectionToConversion(consequentCatalysis.getCatalysisDirection());
						ConversionDirectionType dirB =
								findConsensusDirection(nextConversion.getConversionDirection(), direction2);

						// Ensure intermediate molecule (pe) is input to the neighbor conversion
						if (commonSubstrateFollowsFlow(pe, nextConversion, dirB))
						{
							for (Controller controller : consequentCatalysis.getController())
							{
								if (controller instanceof PhysicalEntity)
								{
									createSimpleInteraction(bpe, interactionSet, (PhysicalEntity) controller,
									                        aCatalysis, consequentCatalysis);
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean commonSubstrateFollowsFlow(PhysicalEntity pe, Conversion nextConversion,
			ConversionDirectionType dirB)
	{
		if(dirB==null) return true; //Assume reversible
		switch (dirB)
		{
			case REVERSIBLE:return true;
			case LEFT_TO_RIGHT:return nextConversion.getLeft().contains(pe);
			case RIGHT_TO_LEFT:return nextConversion.getRight().contains(pe);
			default: throw new IllegalBioPAXArgumentException(); // Should never hit here
		}
	}

	private void createSimpleInteraction(BioPAXElement source, InteractionSetL3 interactionSet,
			PhysicalEntity controller, Catalysis firstCatalysis, Catalysis consequentCatalysis)
	{

		{
			BioPAXElement target = this.getEntityReferenceOrGroup(controller, interactionSet);
			SimpleInteraction si = new SimpleInteraction(source, target, SEQUENTIAL_CATALYSIS);
			interactionSet.add(si);
			si.addMediator(firstCatalysis);
			si.addMediator(consequentCatalysis);
		}
	}

	/**
	 * This method returns the PEPs that are on the correct side of the conversion
	 * @param direction determining the side
	 * @param aConversion
	 * @return
	 */
	private Set<PhysicalEntity> getOutputPEs(ConversionDirectionType direction, Conversion aConversion)
	{
		switch (direction)
		{
			case LEFT_TO_RIGHT:
				return aConversion.getRight();
			case RIGHT_TO_LEFT:
				return aConversion.getLeft();
			default:
				return mergedSet(aConversion);
		}
	}

	private HashSet<PhysicalEntity> mergedSet(Conversion aConversion)
	{
		HashSet<PhysicalEntity> hashSet = new HashSet<PhysicalEntity>();
		hashSet.addAll(aConversion.getLeft());
		hashSet.addAll(aConversion.getRight());
		return hashSet;
	}

	private ConversionDirectionType mapDirectionToConversion(CatalysisDirectionType direction)
	{
		if (direction != null)
		{
			switch (direction)
			{
				case LEFT_TO_RIGHT:
					return ConversionDirectionType.LEFT_TO_RIGHT;
				case RIGHT_TO_LEFT:
					return ConversionDirectionType.RIGHT_TO_LEFT;
			}
		}
		return null;
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}