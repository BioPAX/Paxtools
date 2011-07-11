package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.*;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.SEQUENTIAL_CATALYSIS;

/**
 * This class creates an interaction between two entities if they are catalyzing consecutive
 * conversions. Conversions are considered consecutive if one of the RIGHT participants of one
 * reaction is the LEFT of the other and if the directions of catalysis and control matches. User:
 * demir Date: Dec 28, 2007 Time: 10:40:01 PM
 */
public class ConsecutiveCatalysisRule implements InteractionRuleL3
{
	public void inferInteractions(Set<SimpleInteraction> interactionSet, Object entity,
	                              Model model, Map options)
	{
		inferInteractions(interactionSet, ((EntityReference) entity), model, options);
	}

	public void inferInteractions(Set<SimpleInteraction> interactionSet, EntityReference A,
	                              Model model, Map options)
	{
		// Stop if options don't let to continue
		if (options.containsKey(SEQUENTIAL_CATALYSIS) &&
		    options.get(SEQUENTIAL_CATALYSIS).equals(false))
		{
			return;
		}

		// OK, go on...
		for (PhysicalEntity pe : A.getEntityReferenceOf())
		{
			processPhysicalEntity(interactionSet, A, pe);
		}
	}

	private void processPhysicalEntity(Set<SimpleInteraction> interactionSet, EntityReference A,
	                                   PhysicalEntity pe)
	{
		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Catalysis)
			{
				processCatalysis(interactionSet, A, (Catalysis) inter);
			}
		}
		for (Complex comp : pe.getComponentOf())
		{
			processPhysicalEntity(interactionSet, A, comp);
		}
	}

	private void processCatalysis(Set<SimpleInteraction> interactionSet,
	                              EntityReference A,
	                              Catalysis aCatalysis)
	{
		//We have to consider two direction statements
		//Catalysis.direction and Conversion.spontaneous
		//This method maps the former to the compatible latter
		//null means reversible or unknown, both are treated in the same way.
		ConversionDirectionType catalysisDirection =
				mapDirectionToConversion(aCatalysis.getCatalysisDirection());

		//get the conversions and process them.
		Set<Process> controlled = aCatalysis.getControlled();
		for (Process process : controlled)
		{
			assert process instanceof Conversion;

			Conversion aConversion = (Conversion) process;

			//let's find the direction that is compatible with catalysis direction
			ConversionDirectionType dirA = findConsensusDirection(catalysisDirection,
					aConversion.getConversionDirection());

			assert dirA != null;

			//and let's get the interacting physical entities
			createInteractions(aConversion, dirA, A, aCatalysis, interactionSet);
		}
	}

	/**
	 * This method will find the compatible conversion and catalysis direction
	 *
	 * @param direction1 type implied by catalysis
	 * @param direction2 type of the conversion
	 * @return
	 */
	private ConversionDirectionType findConsensusDirection(
			ConversionDirectionType direction1,
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

	private boolean isReversible(ConversionDirectionType direction1)
	{
		return direction1 == null || direction1.equals(ConversionDirectionType.REVERSIBLE);
	}

	private void createInteractions(Conversion centerConversion,
	                                ConversionDirectionType dirA,
	                                EntityReference A,
	                                Catalysis aCatalysis,
	                                Set<SimpleInteraction> interactionSet)
	{
		//get the pes at the correct side of the conversion.
		Set<PhysicalEntity> pes = getOutputPEs(dirA, centerConversion);

		for (PhysicalEntity pe : pes)
		{
			for (Interaction inter : pe.getParticipantOf())
			{
				if (inter instanceof Conversion)
				{
					Conversion neighConv = (Conversion) inter;

					if (centerConversion == neighConv)
					{
						continue;
					}

					Set<Control> controls = neighConv.getControlledOf();

					for (Catalysis consequentCatalysis : new ClassFilterSet<Control,Catalysis>(
							controls, Catalysis.class))
					{
						ConversionDirectionType dirB = findConsensusDirection(
								neighConv.getConversionDirection(),
								mapDirectionToConversion(
										consequentCatalysis.getCatalysisDirection()));

						// Ensure intermediate molecule (pe) is input to the neighbor conversion
						if ((dirB == ConversionDirectionType.LEFT_TO_RIGHT &&
						     neighConv.getLeft().contains(pe)) ||
						    (dirB == ConversionDirectionType.RIGHT_TO_LEFT &&
						     neighConv.getRight().contains(pe)))
						{
							for (Controller controller : consequentCatalysis.getController())
							{
								if (controller instanceof PhysicalEntity)
								{
									createSimpleInteraction(A, interactionSet,
											(PhysicalEntity) controller, aCatalysis,
											consequentCatalysis);
								}
							}
						}
					}
				}
			}
		}
	}

	private void createSimpleInteraction(EntityReference A, Set<SimpleInteraction> interactionSet,
	                                     PhysicalEntity controller, Catalysis firstCatalysis,
	                                     Catalysis consequentCatalysis)
	{
		if (controller instanceof SimplePhysicalEntity)
		{
			//create interactions and add to set
			EntityReference er = ((SimplePhysicalEntity) controller).getEntityReference();
			if(er!=null)
			{
				SimpleInteraction si = new SimpleInteraction(A, er, SEQUENTIAL_CATALYSIS);
				interactionSet.add(si);
			}
		}
		else if (controller instanceof Complex)
		{
			for (EntityReference B : ((Complex) controller).getMemberReferences())
			{
				//create interactions and add to set
				SimpleInteraction si = new SimpleInteraction(A, B, SEQUENTIAL_CATALYSIS);
				interactionSet.add(si);
				si.addMediator(firstCatalysis);
				si.addMediator(consequentCatalysis);
			}
		}
	}

	/**
	 * This method returns the PEPs that are on the correct side of the conversion
	 *
	 * @param direction   determining the side
	 * @param aConversion
	 * @return
	 */
	private Set<PhysicalEntity> getOutputPEs(
			ConversionDirectionType direction,
			Conversion aConversion)
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
		return Arrays.asList(SEQUENTIAL_CATALYSIS);
	}
}