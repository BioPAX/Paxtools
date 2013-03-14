package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;

import java.util.Arrays;
import java.util.List;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.CO_CONTROL;

/**
 * <p/>
 * Implements Co-Control rule, which means A and B have a control over the same interaction.
 * <p/>
 * <p/>
 * This class performs a recursive search both downwards and and upwards of a
 * control that A is a controller. Upwards search always proceed to upwards, i.e.
 * never switch back to downwards at any point. If this is directly upwards of the
 * first control that we start the search, then all reachable B have dependent
 * activity to A. Downward search, however, is different. It can switch to an
 * upwards search when possible, to infer independent controls.
 * <p/>
 * There appears three possible search patterns, starting from the first control
 * (of A), each provides only dependent or independent relations:
 * <p/>
 * <ul>
 * <li>Upwards only --> dependent relation</li>
 * <li>Downwards only --> dependent relation</li>
 * <li>Downwards then upwards --> independent relation</li>
 * </ul>
 * @author Emek Demir
 * @author Ozgun Babur
 */
public class ControlsTogetherRule extends InteractionRuleL2Adaptor
{
	/**
	 * Supported interaction types.
	 */
	private static List<BinaryInteractionType> binaryInteractionTypes = Arrays.asList(CO_CONTROL);

	/**
	 * Infers interactions using A as source.
	 * @param interactionSet to be populated
	 * @param A source
	 * @param model BioPAX model
	 */
	public void inferInteractionsFromPE(InteractionSet interactionSet, physicalEntity A, Model model)
	{

		// Iterate over controls of A
		for (control ctrl : A.getAllInteractions(control.class))
		{
			// Iterate other controllers
			iterateControllers(ctrl, A, interactionSet);
			// Iterate upward controls
			proceedUpwards(ctrl, A, interactionSet);
			// Iterate downward controls
			proceedDownwards(ctrl, A, interactionSet);
		}
	}

	/**
	 * Iterates downwards processes, initiates a downwards search of controls and
	 * upwards search of controls and conversions.
	 * @param ctrl to look downward
	 * @param A first entity in A-B binary interaction
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedDownwards(control ctrl, physicalEntity A, InteractionSet interactionSet)
	{
		// Iterate each controlled process
		for (process prcss : ctrl.getCONTROLLED())
		{
			// Search downwards of controls
			if (prcss instanceof control)
			{
				searchDownwards((control) prcss, A, interactionSet);
			}

			// Search other upwards control trees of all processes (controls and conversions)
			for (control cnt : prcss.isCONTROLLEDOf())
			{
				if (cnt != ctrl) // Do not go where we came from
				{
					searchUpwards(cnt, A, interactionSet);
				}
			}
		}
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds
	 * downwards.
	 * @param ctrl to search downwards
	 * @param A A of A-B
	 * @param interactionSet set to collect inferred interactions
	 */
	private void searchDownwards(control ctrl, physicalEntity A, InteractionSet interactionSet)
	{
		// Search for rules
		iterateControllers(ctrl, A, interactionSet);
		proceedDownwards(ctrl, A, interactionSet);
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds to
	 * upwards.
	 * @param ctrl to search upwards
	 * @param A A in A-B
	 * @param interactionSet set to collect inferred rules
	 */
	private void searchUpwards(control ctrl, physicalEntity A, InteractionSet interactionSet)
	{
		iterateControllers(ctrl, A, interactionSet);
		proceedUpwards(ctrl, A, interactionSet);
	}

	/**
	 * Iterates the controller of ctrl and infers possible binary interactions.
	 * @param ctrl to iterate controllers
	 * @param A A in A-B ctrl
	 * @param interactionSet set to collect inferred interactions
	 */
	private void iterateControllers(control ctrl, physicalEntity A, InteractionSet interactionSet)
	{
		for (physicalEntityParticipant pep : ctrl.getCONTROLLER())
		{
			physicalEntity B = pep.getPHYSICAL_ENTITY();
			if (B != A)
			{
				interactionSet.add(new SimpleInteraction(A, B, CO_CONTROL));
			}
		}
	}

	/**
	 * Iterates upward controls and initiates an upward search for each one.
	 * @param ctrl to proceed upwards
	 * @param A A in A-B
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedUpwards(control ctrl, physicalEntity A, InteractionSet interactionSet)
	{
		for (control cnt : ctrl.isCONTROLLEDOf())
		{
			searchUpwards(cnt, A, interactionSet);
		}
	}

	/**
	 * Gets supported interaction types.
	 * @return supported interaction types
	 */
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}

	
